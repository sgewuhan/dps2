package com.bizvpm.dps.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.sun.net.httpserver.HttpExchange;

@WebService(endpointInterface = "com.bizvpm.dps.server.IPersistence")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class Persistence implements IPersistence, IPersistenceConstants {

	private DB db;

	@Resource
	private WebServiceContext wsContext;

	public Persistence(DB db) {
		this.db = db;
	}

	@Override
	public void updateProcessManagerStatus(PersistableProcessorManager manager, int status) {
		List<PersistableProcessor> configs = manager.getProcessorConfigs();
		if (configs == null || configs.isEmpty()) {
			return;
		}
		String host = manager.getHost();
		DBCollection col = db.getCollection(DPS_COLLECTION);
		DBObject query = new BasicDBObject().append(F_HOSTNAME, host);
		DBObject update = new BasicDBObject().append(F_HOSTNAME, host).append(F_STATUS, status);
		BasicDBList list = new BasicDBList();
		for (int i = 0; i < configs.size(); i++) {
			list.add(getDBObject(configs.get(i)));
		}
		update.put(F_PROCESSORS, list);
		col.update(query, update, true, false);
	}

	private DBObject getDBObject(PersistableProcessor config) {
		BasicDBObject result = new BasicDBObject();
		result.put(F_PROC_ID, config.getId());
		result.put(F_PROC_NAME, config.getName());
		result.put(F_DESC, config.getDescritpion());
		result.put(F_PROC_NAME, config.getName());
		result.put(F_ONLINE, config.isOnline());
		ParameterList list = config.getParameterList();
		if (list != null) {
			BasicDBList dparas = new BasicDBList();
			List<Parameter> parameters = list.getParameters();
			if (parameters != null) {
				for (int i = 0; i < parameters.size(); i++) {
					Parameter parameter = parameters.get(i);
					BasicDBObject dpara = new BasicDBObject();
					dpara.put(F_PARAMETER_NAME, parameter.getName());
					dpara.put(F_PARAMETER_TYPE, parameter.getType());
					dpara.put(F_PARAMETER_RESTICATIONS, parameter.getRestrictions());
					dpara.put(F_DESC, parameter.getDescription());
					dparas.add(dpara);
				}
			}
			result.put(F_PARAMETERS, dparas);
		}
		return result;
	}

	@Override
	public void updateProcessManagerStatusByHosts(StringList hostList, int status) {
		DBCollection col = db.getCollection(DPS_COLLECTION);
		DBObject query = new BasicDBObject().append(F_HOSTNAME, new BasicDBObject().append("$in", hostList.getItems()));
		DBObject update = new BasicDBObject().append("$set", new BasicDBObject().append(F_STATUS, status));
		col.update(query, update, true, false);
	}

	@Override
	public ProcessorList getProcessorList(int status) {
		DBCollection col = db.getCollection(DPS_COLLECTION);
		DBCursor cur = col.find(new BasicDBObject().append(F_STATUS, status));
		ProcessorList result = new ProcessorList();
		List<PersistableProcessor> items = new ArrayList<PersistableProcessor>();
		while (cur.hasNext()) {
			DBObject manager = cur.next();
			String url = (String) manager.get(F_HOSTNAME);
			List<?> processors = (List<?>) manager.get(F_PROCESSORS);
			if (processors != null && processors.size() > 0) {
				for (int i = 0; i < processors.size(); i++) {
					DBObject processor = (DBObject) processors.get(i);
					PersistableProcessor pp = new PersistableProcessor();
					pp.setManagerUrl(url);
					pp.setId((String) processor.get(F_PROC_ID));
					pp.setName((String) processor.get(F_PROC_NAME));
					items.add(pp);
				}
			}
		}
		result.setItems(items);
		cur.close();
		return result;
	}

	@Override
	public StringList getOnlineProcessManager(String processortypeId) {
		DBCollection col = db.getCollection(DPS_COLLECTION);
		BasicDBObject query = new BasicDBObject().append(F_PROCESSORS + "." + F_PROC_ID, processortypeId)
				.append(F_PROCESSORS + "." + F_ONLINE, Boolean.TRUE).append(F_STATUS, 1);
		DBCursor cur = col.find(query);
		List<String> items = new ArrayList<String>();
		while (cur.hasNext()) {
			DBObject data = cur.next();
			items.add((String) data.get(F_HOSTNAME));
		}
		cur.close();
		StringList hostList = new StringList();
		hostList.setItems(items);
		return hostList;
	}

	@Override
	public void updateTaskStatus(String taskId, Result result, String status) {
		DBCollection col = db.getCollection(DPS_TASK_COLLECTION);
		col.update(new BasicDBObject().append(F__ID, new ObjectId(taskId)),
				new BasicDBObject().append("$set", new BasicDBObject().append(F_STATUS, status)
						.append(status + "date", new Date()).append(F_RESULT, getDBObject(result))),
				true, false);
	}

	@Override
	public void updateTaskStatusWithMessage(String taskId, Result result, String status, String message) {
		DBCollection col = db.getCollection(DPS_TASK_COLLECTION);
		col.update(new BasicDBObject().append(F__ID, new ObjectId(taskId)),
				new BasicDBObject()
						.append("$set",
								new BasicDBObject().append(F_STATUS, status).append(status + "date", new Date())
										.append(F_RESULT, getDBObject(result)).append(F_MESSAGE, message)),
				true, false);

	}

	@Override
	public void removeTask(String taskId) {
		BasicDBObject query = new BasicDBObject().append(F__ID, new ObjectId(taskId));
		removeTask(query);
	}

	@Override
	public void removeTaskListByProcessorTypeId(String processorTypeId) {
		BasicDBObject query = new BasicDBObject().append(F_PROCESSOR_TYPE_ID, processorTypeId);
		removeTask(query);
	}

	@Override
	public void removeTaskList(StringList StringList) {
		List<String> items = StringList.getItems();
		if (items == null || items.isEmpty()) {
			return;
		}
		List<ObjectId> ids = new ArrayList<ObjectId>();
		for (int i = 0; i < items.size(); i++) {
			ids.add(new ObjectId(items.get(i)));
		}
		BasicDBObject query = new BasicDBObject().append(F__ID, new BasicDBObject().append("$in", ids));

		removeTask(query);
	}

	private void removeTask(BasicDBObject query) {
		DBCollection col = db.getCollection(DPS_TASK_COLLECTION);
		List<ObjectId> fids = new ArrayList<ObjectId>();
		DBCursor cursor = col.find(query);
		DBObject data;
		while (cursor.hasNext()) {
			data = cursor.next();
			Object task = data.get(F_TASK_DATA);
			if (task instanceof DBObject) {
				if ("file".equals(((DBObject) task).get("type"))) {
					fids.add((ObjectId) ((DBObject) task).get("_id"));
				}
			}
		}
		cursor.close();
		GridFS gridfs = new GridFS(db, GRID_FS_TASK_FILE);
		gridfs.remove(new BasicDBObject().append("_id", new BasicDBObject().append("$in", fids)));
		col.remove(query);
	}

	@Override
	public String createTask(String host, String processorTypeId, Task task) {
		DBCollection col = db.getCollection(DPS_TASK_COLLECTION);
		DBObject document = new BasicDBObject();
		ObjectId _id = new ObjectId();
		document.put(F__ID, _id);
		document.put(F_HOSTNAME, host);
		document.put(F_PROCESSOR_TYPE_ID, processorTypeId);
		document.put(F_STATUS, "received");
		document.put(F_TASK_NAME, task.getName());
		document.put(F_TASK_PRIORITY, task.getPriority());
		document.put("receiveddate", new Date());
		String oid = (String) task.getParentId();
		if (oid != null) {
			ObjectId parent_id = new ObjectId(oid);
			document.put(F_PARENTID, parent_id);
		}
		document.put(F_CLIENT_IP, task.getClientIp());
		document.put(F_CLIENT_NAME, task.getClientName());

		DBObject taskData = getDBObject(task);

		document.put(F_TASK_DATA, taskData);
		col.insert(document);
		return _id.toHexString();
	}

	private DBObject getDBObject(DataSet dataSet) {
		DataObjectConverter converter = new DBValueConverter(db);
		DBObject taskData = new BasicDBObject();
		List<KeyValuePair> values = dataSet.getValues();
		for (KeyValuePair keyValuePair : values) {
			DataObject data = keyValuePair.getValue();

			Object value = converter.getValue(data);
			String key = keyValuePair.getKey();
			taskData.put(key, value);
		}
		return taskData;
	}

	@Override
	public TaskList getTaskListByProcessorTypeId(String host, String processorTypeId) {
		List<PersistableTask> tasks = new ArrayList<PersistableTask>();
		DBCollection col = db.getCollection(DPS_TASK_COLLECTION);
		DBCursor cur = col
				.find(new BasicDBObject().append(F_HOSTNAME, host).append(F_PROCESSOR_TYPE_ID, processorTypeId));
		cur.sort(new BasicDBObject().append("receiveddate", -1));
		while (cur.hasNext()) {
			DBObject data = cur.next();
			PersistableTask pt = getTask(data);
			tasks.add(pt);
		}
		cur.close();
		TaskList result = new TaskList();
		result.setTasks(tasks);
		return result;
	}

	@Override
	public TaskList getTaskList(String host) {
		List<PersistableTask> tasks = new ArrayList<PersistableTask>();
		DBCollection col = db.getCollection(DPS_TASK_COLLECTION);
		DBCursor cur = col.find(new BasicDBObject().append(F_HOSTNAME, host));
		while (cur.hasNext()) {
			DBObject data = cur.next();
			PersistableTask pt = getTask(data);
			tasks.add(pt);
		}
		cur.close();
		TaskList result = new TaskList();
		result.setTasks(tasks);
		return result;
	}

	private PersistableTask getTask(DBObject data) {
		PersistableTask pt = new PersistableTask();
		pt.setId(((ObjectId) data.get(F__ID)).toHexString());
		pt.setProcessorTypeId((String) data.get(F_PROCESSOR_TYPE_ID));
		pt.setName((String) data.get(F_TASK_NAME));
		pt.setPriority((Integer) data.get(F_TASK_PRIORITY));
		Object parentId = data.get(F_PARENTID);
		if (parentId instanceof ObjectId) {
			pt.setParentId(parentId.toString());
		}
		pt.setClientName((String) data.get(F_CLIENT_NAME));
		pt.setClientIp((String) data.get(F_CLIENT_IP));
		pt.setStatus((String) data.get(F_STATUS));
		pt.setReceivedDate((Date) data.get("receiveddate"));
		pt.setDoneDate((Date) data.get("donedate"));
		pt.setErrorDate((Date) data.get("errordate"));
		pt.setMessage((String) data.get(F_MESSAGE));

		DBObject taskData = (DBObject) data.get(F_TASK_DATA);
		List<KeyValuePair> values = pt.getValues();

		DBValueConverter converter = new DBValueConverter(db);
		Iterator<String> iter = taskData.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			Object value = taskData.get(key);
			KeyValuePair kv = new KeyValuePair();
			kv.setKey(key);
			kv.setValue(converter.getDataObject(value));
			values.add(kv);
		}

		DBObject resultData = (DBObject) data.get(F_RESULT);
		List<KeyValuePair> result = new ArrayList<KeyValuePair>();

		if (resultData != null) {
			iter = resultData.keySet().iterator();
			while (iter.hasNext()) {
				String key = iter.next();
				Object value = resultData.get(key);
				KeyValuePair kv = new KeyValuePair();
				kv.setKey(key);
				kv.setValue(converter.getDataObject(value));
				result.add(kv);
			}
		}
		pt.setResult(result);

		return pt;
	}

	@Override
	public boolean signin(String host, String password, String hostIp, int hostPort) {
		DBCollection col = db.getCollection(DPS_ACCOUNT);
		Object pattern = Pattern.compile(host, Pattern.CASE_INSENSITIVE);
		DBObject query = new BasicDBObject();
		query.put(F_HOSTNAME, pattern);
		query.put(F_PASSWORD, password);
		DBObject d = col.findOne(query);
		if (d != null) {
			query = new BasicDBObject().append(F__ID, d.get(F__ID));
			BasicDBObject update = new BasicDBObject();
			update.append("$set", new BasicDBObject().append(F_HOSTIP, hostIp).append(F_HOSTPORT, hostPort));
			col.update(query, update, false, false);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean signup(String host, String password, String hostIp, int hostPort) {
		DBCollection col = db.getCollection(DPS_ACCOUNT);
		Object pattern = Pattern.compile(host, Pattern.CASE_INSENSITIVE);
		DBObject query = new BasicDBObject();
		query.put(F_HOSTNAME, pattern);
		query.put(F_PASSWORD, password);
		DBObject d = col.findOne(query);
		if (d != null) {
			return false;
		} else {
			BasicDBObject insert = new BasicDBObject();
			insert.put(F_HOSTNAME, host);
			insert.put(F_PASSWORD, password);
			insert.put(F_HOSTIP, hostIp);
			insert.put(F_HOSTPORT, hostPort);
			col.insert(insert);
			return true;
		}
	}

	@Override
	public String getHostAddress(String host) {
		DBCollection col = db.getCollection(DPS_ACCOUNT);
		Object pattern = Pattern.compile(host, Pattern.CASE_INSENSITIVE);
		DBObject query = new BasicDBObject();
		query.put(F_HOSTNAME, pattern);
		DBObject d = col.findOne(query);
		if (d != null) {
			return d.get(F_HOSTIP) + ":" + d.get(F_HOSTPORT);
		} else {
			return "";
		}
	}

	@Override
	public String ping() {
		MessageContext mc = wsContext.getMessageContext();
		HttpExchange client = (HttpExchange) mc.get("com.sun.xml.internal.ws.http.exchange");
		return client.getRemoteAddress().getAddress().getHostAddress();
	}

}
