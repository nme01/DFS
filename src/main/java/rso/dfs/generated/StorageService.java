package rso.dfs.generated;

/**
 * @generated
 * 
 * */

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.server.AbstractNonblockingServer.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageService {

	public interface Iface {

		public void putFile(int fileId, List<Byte> body)
				throws org.apache.thrift.TException;

		public List<Byte> getFile(int fileId)
				throws org.apache.thrift.TException;

	}

	public interface AsyncIface {

		public void putFile(int fileId, List<Byte> body,
				org.apache.thrift.async.AsyncMethodCallback resultHandler)
				throws org.apache.thrift.TException;

		public void getFile(int fileId,
				org.apache.thrift.async.AsyncMethodCallback resultHandler)
				throws org.apache.thrift.TException;

	}

	public static class Client extends org.apache.thrift.TServiceClient
			implements Iface {
		public static class Factory implements
				org.apache.thrift.TServiceClientFactory<Client> {
			public Factory() {
			}

			public Client getClient(org.apache.thrift.protocol.TProtocol prot) {
				return new Client(prot);
			}

			public Client getClient(org.apache.thrift.protocol.TProtocol iprot,
					org.apache.thrift.protocol.TProtocol oprot) {
				return new Client(iprot, oprot);
			}
		}

		public Client(org.apache.thrift.protocol.TProtocol prot) {
			super(prot, prot);
		}

		public Client(org.apache.thrift.protocol.TProtocol iprot,
				org.apache.thrift.protocol.TProtocol oprot) {
			super(iprot, oprot);
		}

		public void putFile(int fileId, List<Byte> body)
				throws org.apache.thrift.TException {
			send_putFile(fileId, body);
			recv_putFile();
		}

		public void send_putFile(int fileId, List<Byte> body)
				throws org.apache.thrift.TException {
			putFile_args args = new putFile_args();
			args.setFileId(fileId);
			args.setBody(body);
			sendBase("putFile", args);
		}

		public void recv_putFile() throws org.apache.thrift.TException {
			putFile_result result = new putFile_result();
			receiveBase(result, "putFile");
			return;
		}

		public List<Byte> getFile(int fileId)
				throws org.apache.thrift.TException {
			send_getFile(fileId);
			return recv_getFile();
		}

		public void send_getFile(int fileId)
				throws org.apache.thrift.TException {
			getFile_args args = new getFile_args();
			args.setFileId(fileId);
			sendBase("getFile", args);
		}

		public List<Byte> recv_getFile() throws org.apache.thrift.TException {
			getFile_result result = new getFile_result();
			receiveBase(result, "getFile");
			if (result.isSetSuccess()) {
				return result.success;
			}
			throw new org.apache.thrift.TApplicationException(
					org.apache.thrift.TApplicationException.MISSING_RESULT,
					"getFile failed: unknown result");
		}

	}

	public static class AsyncClient extends
			org.apache.thrift.async.TAsyncClient implements AsyncIface {
		public static class Factory implements
				org.apache.thrift.async.TAsyncClientFactory<AsyncClient> {
			private org.apache.thrift.async.TAsyncClientManager clientManager;
			private org.apache.thrift.protocol.TProtocolFactory protocolFactory;

			public Factory(
					org.apache.thrift.async.TAsyncClientManager clientManager,
					org.apache.thrift.protocol.TProtocolFactory protocolFactory) {
				this.clientManager = clientManager;
				this.protocolFactory = protocolFactory;
			}

			public AsyncClient getAsyncClient(
					org.apache.thrift.transport.TNonblockingTransport transport) {
				return new AsyncClient(protocolFactory, clientManager,
						transport);
			}
		}

		public AsyncClient(
				org.apache.thrift.protocol.TProtocolFactory protocolFactory,
				org.apache.thrift.async.TAsyncClientManager clientManager,
				org.apache.thrift.transport.TNonblockingTransport transport) {
			super(protocolFactory, clientManager, transport);
		}

		public void putFile(int fileId, List<Byte> body,
				org.apache.thrift.async.AsyncMethodCallback resultHandler)
				throws org.apache.thrift.TException {
			checkReady();
			putFile_call method_call = new putFile_call(fileId, body,
					resultHandler, this, ___protocolFactory, ___transport);
			this.___currentMethod = method_call;
			___manager.call(method_call);
		}

		public static class putFile_call extends
				org.apache.thrift.async.TAsyncMethodCall {
			private int fileId;
			private List<Byte> body;

			public putFile_call(
					int fileId,
					List<Byte> body,
					org.apache.thrift.async.AsyncMethodCallback resultHandler,
					org.apache.thrift.async.TAsyncClient client,
					org.apache.thrift.protocol.TProtocolFactory protocolFactory,
					org.apache.thrift.transport.TNonblockingTransport transport)
					throws org.apache.thrift.TException {
				super(client, protocolFactory, transport, resultHandler, false);
				this.fileId = fileId;
				this.body = body;
			}

			public void write_args(org.apache.thrift.protocol.TProtocol prot)
					throws org.apache.thrift.TException {
				prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage(
						"putFile",
						org.apache.thrift.protocol.TMessageType.CALL, 0));
				putFile_args args = new putFile_args();
				args.setFileId(fileId);
				args.setBody(body);
				args.write(prot);
				prot.writeMessageEnd();
			}

			public void getResult() throws org.apache.thrift.TException {
				if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
					throw new IllegalStateException("Method call not finished!");
				}
				org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(
						getFrameBuffer().array());
				org.apache.thrift.protocol.TProtocol prot = client
						.getProtocolFactory().getProtocol(memoryTransport);
				(new Client(prot)).recv_putFile();
			}
		}

		public void getFile(int fileId,
				org.apache.thrift.async.AsyncMethodCallback resultHandler)
				throws org.apache.thrift.TException {
			checkReady();
			getFile_call method_call = new getFile_call(fileId, resultHandler,
					this, ___protocolFactory, ___transport);
			this.___currentMethod = method_call;
			___manager.call(method_call);
		}

		public static class getFile_call extends
				org.apache.thrift.async.TAsyncMethodCall {
			private int fileId;

			public getFile_call(
					int fileId,
					org.apache.thrift.async.AsyncMethodCallback resultHandler,
					org.apache.thrift.async.TAsyncClient client,
					org.apache.thrift.protocol.TProtocolFactory protocolFactory,
					org.apache.thrift.transport.TNonblockingTransport transport)
					throws org.apache.thrift.TException {
				super(client, protocolFactory, transport, resultHandler, false);
				this.fileId = fileId;
			}

			public void write_args(org.apache.thrift.protocol.TProtocol prot)
					throws org.apache.thrift.TException {
				prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage(
						"getFile",
						org.apache.thrift.protocol.TMessageType.CALL, 0));
				getFile_args args = new getFile_args();
				args.setFileId(fileId);
				args.write(prot);
				prot.writeMessageEnd();
			}

			public List<Byte> getResult() throws org.apache.thrift.TException {
				if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
					throw new IllegalStateException("Method call not finished!");
				}
				org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(
						getFrameBuffer().array());
				org.apache.thrift.protocol.TProtocol prot = client
						.getProtocolFactory().getProtocol(memoryTransport);
				return (new Client(prot)).recv_getFile();
			}
		}

	}

	public static class Processor<I extends Iface> extends
			org.apache.thrift.TBaseProcessor<I> implements
			org.apache.thrift.TProcessor {
		private static final Logger LOGGER = LoggerFactory
				.getLogger(Processor.class.getName());

		public Processor(I iface) {
			super(
					iface,
					getProcessMap(new HashMap<String, org.apache.thrift.ProcessFunction<I, ? extends org.apache.thrift.TBase>>()));
		}

		protected Processor(
				I iface,
				Map<String, org.apache.thrift.ProcessFunction<I, ? extends org.apache.thrift.TBase>> processMap) {
			super(iface, getProcessMap(processMap));
		}

		private static <I extends Iface> Map<String, org.apache.thrift.ProcessFunction<I, ? extends org.apache.thrift.TBase>> getProcessMap(
				Map<String, org.apache.thrift.ProcessFunction<I, ? extends org.apache.thrift.TBase>> processMap) {
			processMap.put("putFile", new putFile());
			processMap.put("getFile", new getFile());
			return processMap;
		}

		public static class putFile<I extends Iface> extends
				org.apache.thrift.ProcessFunction<I, putFile_args> {
			public putFile() {
				super("putFile");
			}

			public putFile_args getEmptyArgsInstance() {
				return new putFile_args();
			}

			protected boolean isOneway() {
				return false;
			}

			public putFile_result getResult(I iface, putFile_args args)
					throws org.apache.thrift.TException {
				putFile_result result = new putFile_result();
				iface.putFile(args.fileId, args.body);
				return result;
			}
		}

		public static class getFile<I extends Iface> extends
				org.apache.thrift.ProcessFunction<I, getFile_args> {
			public getFile() {
				super("getFile");
			}

			public getFile_args getEmptyArgsInstance() {
				return new getFile_args();
			}

			protected boolean isOneway() {
				return false;
			}

			public getFile_result getResult(I iface, getFile_args args)
					throws org.apache.thrift.TException {
				getFile_result result = new getFile_result();
				result.success = iface.getFile(args.fileId);
				return result;
			}
		}

	}

	public static class AsyncProcessor<I extends AsyncIface> extends
			org.apache.thrift.TBaseAsyncProcessor<I> {
		private static final Logger LOGGER = LoggerFactory
				.getLogger(AsyncProcessor.class.getName());

		public AsyncProcessor(I iface) {
			super(
					iface,
					getProcessMap(new HashMap<String, org.apache.thrift.AsyncProcessFunction<I, ? extends org.apache.thrift.TBase, ?>>()));
		}

		protected AsyncProcessor(
				I iface,
				Map<String, org.apache.thrift.AsyncProcessFunction<I, ? extends org.apache.thrift.TBase, ?>> processMap) {
			super(iface, getProcessMap(processMap));
		}

		private static <I extends AsyncIface> Map<String, org.apache.thrift.AsyncProcessFunction<I, ? extends org.apache.thrift.TBase, ?>> getProcessMap(
				Map<String, org.apache.thrift.AsyncProcessFunction<I, ? extends org.apache.thrift.TBase, ?>> processMap) {
			processMap.put("putFile", new putFile());
			processMap.put("getFile", new getFile());
			return processMap;
		}

		public static class putFile<I extends AsyncIface> extends
				org.apache.thrift.AsyncProcessFunction<I, putFile_args, Void> {
			public putFile() {
				super("putFile");
			}

			public putFile_args getEmptyArgsInstance() {
				return new putFile_args();
			}

			public AsyncMethodCallback<Void> getResultHandler(
					final AsyncFrameBuffer fb, final int seqid) {
				final org.apache.thrift.AsyncProcessFunction fcall = this;
				return new AsyncMethodCallback<Void>() {
					public void onComplete(Void o) {
						putFile_result result = new putFile_result();
						try {
							fcall.sendResponse(
									fb,
									result,
									org.apache.thrift.protocol.TMessageType.REPLY,
									seqid);
							return;
						} catch (Exception e) {
							LOGGER.error(
									"Exception writing to internal frame buffer",
									e);
						}
						fb.close();
					}

					public void onError(Exception e) {
						byte msgType = org.apache.thrift.protocol.TMessageType.REPLY;
						org.apache.thrift.TBase msg;
						putFile_result result = new putFile_result();
						{
							msgType = org.apache.thrift.protocol.TMessageType.EXCEPTION;
							msg = (org.apache.thrift.TBase) new org.apache.thrift.TApplicationException(
									org.apache.thrift.TApplicationException.INTERNAL_ERROR,
									e.getMessage());
						}
						try {
							fcall.sendResponse(fb, msg, msgType, seqid);
							return;
						} catch (Exception ex) {
							LOGGER.error(
									"Exception writing to internal frame buffer",
									ex);
						}
						fb.close();
					}
				};
			}

			protected boolean isOneway() {
				return false;
			}

			public void start(
					I iface,
					putFile_args args,
					org.apache.thrift.async.AsyncMethodCallback<Void> resultHandler)
					throws TException {
				iface.putFile(args.fileId, args.body, resultHandler);
			}
		}

		public static class getFile<I extends AsyncIface>
				extends
				org.apache.thrift.AsyncProcessFunction<I, getFile_args, List<Byte>> {
			public getFile() {
				super("getFile");
			}

			public getFile_args getEmptyArgsInstance() {
				return new getFile_args();
			}

			public AsyncMethodCallback<List<Byte>> getResultHandler(
					final AsyncFrameBuffer fb, final int seqid) {
				final org.apache.thrift.AsyncProcessFunction fcall = this;
				return new AsyncMethodCallback<List<Byte>>() {
					public void onComplete(List<Byte> o) {
						getFile_result result = new getFile_result();
						result.success = o;
						try {
							fcall.sendResponse(
									fb,
									result,
									org.apache.thrift.protocol.TMessageType.REPLY,
									seqid);
							return;
						} catch (Exception e) {
							LOGGER.error(
									"Exception writing to internal frame buffer",
									e);
						}
						fb.close();
					}

					public void onError(Exception e) {
						byte msgType = org.apache.thrift.protocol.TMessageType.REPLY;
						org.apache.thrift.TBase msg;
						getFile_result result = new getFile_result();
						{
							msgType = org.apache.thrift.protocol.TMessageType.EXCEPTION;
							msg = (org.apache.thrift.TBase) new org.apache.thrift.TApplicationException(
									org.apache.thrift.TApplicationException.INTERNAL_ERROR,
									e.getMessage());
						}
						try {
							fcall.sendResponse(fb, msg, msgType, seqid);
							return;
						} catch (Exception ex) {
							LOGGER.error(
									"Exception writing to internal frame buffer",
									ex);
						}
						fb.close();
					}
				};
			}

			protected boolean isOneway() {
				return false;
			}

			public void start(
					I iface,
					getFile_args args,
					org.apache.thrift.async.AsyncMethodCallback<List<Byte>> resultHandler)
					throws TException {
				iface.getFile(args.fileId, resultHandler);
			}
		}

	}

	public static class putFile_args implements
			org.apache.thrift.TBase<putFile_args, putFile_args._Fields>,
			java.io.Serializable, Cloneable, Comparable<putFile_args> {
		private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct(
				"putFile_args");

		private static final org.apache.thrift.protocol.TField FILE_ID_FIELD_DESC = new org.apache.thrift.protocol.TField(
				"fileId", org.apache.thrift.protocol.TType.I32, (short) 1);
		private static final org.apache.thrift.protocol.TField BODY_FIELD_DESC = new org.apache.thrift.protocol.TField(
				"body", org.apache.thrift.protocol.TType.LIST, (short) 2);

		private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
		static {
			schemes.put(StandardScheme.class,
					new putFile_argsStandardSchemeFactory());
			schemes.put(TupleScheme.class, new putFile_argsTupleSchemeFactory());
		}

		public int fileId; // required
		public List<Byte> body; // required

		/**
		 * The set of fields this struct contains, along with convenience
		 * methods for finding and manipulating them.
		 */
		public enum _Fields implements org.apache.thrift.TFieldIdEnum {
			FILE_ID((short) 1, "fileId"), BODY((short) 2, "body");

			private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

			static {
				for (_Fields field : EnumSet.allOf(_Fields.class)) {
					byName.put(field.getFieldName(), field);
				}
			}

			/**
			 * Find the _Fields constant that matches fieldId, or null if its
			 * not found.
			 */
			public static _Fields findByThriftId(int fieldId) {
				switch (fieldId) {
				case 1: // FILE_ID
					return FILE_ID;
				case 2: // BODY
					return BODY;
				default:
					return null;
				}
			}

			/**
			 * Find the _Fields constant that matches fieldId, throwing an
			 * exception if it is not found.
			 */
			public static _Fields findByThriftIdOrThrow(int fieldId) {
				_Fields fields = findByThriftId(fieldId);
				if (fields == null)
					throw new IllegalArgumentException("Field " + fieldId
							+ " doesn't exist!");
				return fields;
			}

			/**
			 * Find the _Fields constant that matches name, or null if its not
			 * found.
			 */
			public static _Fields findByName(String name) {
				return byName.get(name);
			}

			private final short _thriftId;
			private final String _fieldName;

			_Fields(short thriftId, String fieldName) {
				_thriftId = thriftId;
				_fieldName = fieldName;
			}

			public short getThriftFieldId() {
				return _thriftId;
			}

			public String getFieldName() {
				return _fieldName;
			}
		}

		// isset id assignments
		private static final int __FILEID_ISSET_ID = 0;
		private byte __isset_bitfield = 0;
		public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
		static {
			Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(
					_Fields.class);
			tmpMap.put(
					_Fields.FILE_ID,
					new org.apache.thrift.meta_data.FieldMetaData(
							"fileId",
							org.apache.thrift.TFieldRequirementType.DEFAULT,
							new org.apache.thrift.meta_data.FieldValueMetaData(
									org.apache.thrift.protocol.TType.I32, "int")));
			tmpMap.put(
					_Fields.BODY,
					new org.apache.thrift.meta_data.FieldMetaData(
							"body",
							org.apache.thrift.TFieldRequirementType.DEFAULT,
							new org.apache.thrift.meta_data.ListMetaData(
									org.apache.thrift.protocol.TType.LIST,
									new org.apache.thrift.meta_data.FieldValueMetaData(
											org.apache.thrift.protocol.TType.BYTE))));
			metaDataMap = Collections.unmodifiableMap(tmpMap);
			org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(
					putFile_args.class, metaDataMap);
		}

		public putFile_args() {
		}

		public putFile_args(int fileId, List<Byte> body) {
			this();
			this.fileId = fileId;
			setFileIdIsSet(true);
			this.body = body;
		}

		/**
		 * Performs a deep copy on <i>other</i>.
		 */
		public putFile_args(putFile_args other) {
			__isset_bitfield = other.__isset_bitfield;
			this.fileId = other.fileId;
			if (other.isSetBody()) {
				List<Byte> __this__body = new ArrayList<Byte>(other.body);
				this.body = __this__body;
			}
		}

		public putFile_args deepCopy() {
			return new putFile_args(this);
		}

		@Override
		public void clear() {
			setFileIdIsSet(false);
			this.fileId = 0;
			this.body = null;
		}

		public int getFileId() {
			return this.fileId;
		}

		public putFile_args setFileId(int fileId) {
			this.fileId = fileId;
			setFileIdIsSet(true);
			return this;
		}

		public void unsetFileId() {
			__isset_bitfield = EncodingUtils.clearBit(__isset_bitfield,
					__FILEID_ISSET_ID);
		}

		/**
		 * Returns true if field fileId is set (has been assigned a value) and
		 * false otherwise
		 */
		public boolean isSetFileId() {
			return EncodingUtils.testBit(__isset_bitfield, __FILEID_ISSET_ID);
		}

		public void setFileIdIsSet(boolean value) {
			__isset_bitfield = EncodingUtils.setBit(__isset_bitfield,
					__FILEID_ISSET_ID, value);
		}

		public int getBodySize() {
			return (this.body == null) ? 0 : this.body.size();
		}

		public java.util.Iterator<Byte> getBodyIterator() {
			return (this.body == null) ? null : this.body.iterator();
		}

		public void addToBody(byte elem) {
			if (this.body == null) {
				this.body = new ArrayList<Byte>();
			}
			this.body.add(elem);
		}

		public List<Byte> getBody() {
			return this.body;
		}

		public putFile_args setBody(List<Byte> body) {
			this.body = body;
			return this;
		}

		public void unsetBody() {
			this.body = null;
		}

		/**
		 * Returns true if field body is set (has been assigned a value) and
		 * false otherwise
		 */
		public boolean isSetBody() {
			return this.body != null;
		}

		public void setBodyIsSet(boolean value) {
			if (!value) {
				this.body = null;
			}
		}

		public void setFieldValue(_Fields field, Object value) {
			switch (field) {
			case FILE_ID:
				if (value == null) {
					unsetFileId();
				} else {
					setFileId((Integer) value);
				}
				break;

			case BODY:
				if (value == null) {
					unsetBody();
				} else {
					setBody((List<Byte>) value);
				}
				break;

			}
		}

		public Object getFieldValue(_Fields field) {
			switch (field) {
			case FILE_ID:
				return Integer.valueOf(getFileId());

			case BODY:
				return getBody();

			}
			throw new IllegalStateException();
		}

		/**
		 * Returns true if field corresponding to fieldID is set (has been
		 * assigned a value) and false otherwise
		 */
		public boolean isSet(_Fields field) {
			if (field == null) {
				throw new IllegalArgumentException();
			}

			switch (field) {
			case FILE_ID:
				return isSetFileId();
			case BODY:
				return isSetBody();
			}
			throw new IllegalStateException();
		}

		@Override
		public boolean equals(Object that) {
			if (that == null)
				return false;
			if (that instanceof putFile_args)
				return this.equals((putFile_args) that);
			return false;
		}

		public boolean equals(putFile_args that) {
			if (that == null)
				return false;

			boolean this_present_fileId = true;
			boolean that_present_fileId = true;
			if (this_present_fileId || that_present_fileId) {
				if (!(this_present_fileId && that_present_fileId))
					return false;
				if (this.fileId != that.fileId)
					return false;
			}

			boolean this_present_body = true && this.isSetBody();
			boolean that_present_body = true && that.isSetBody();
			if (this_present_body || that_present_body) {
				if (!(this_present_body && that_present_body))
					return false;
				if (!this.body.equals(that.body))
					return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public int compareTo(putFile_args other) {
			if (!getClass().equals(other.getClass())) {
				return getClass().getName().compareTo(
						other.getClass().getName());
			}

			int lastComparison = 0;

			lastComparison = Boolean.valueOf(isSetFileId()).compareTo(
					other.isSetFileId());
			if (lastComparison != 0) {
				return lastComparison;
			}
			if (isSetFileId()) {
				lastComparison = org.apache.thrift.TBaseHelper.compareTo(
						this.fileId, other.fileId);
				if (lastComparison != 0) {
					return lastComparison;
				}
			}
			lastComparison = Boolean.valueOf(isSetBody()).compareTo(
					other.isSetBody());
			if (lastComparison != 0) {
				return lastComparison;
			}
			if (isSetBody()) {
				lastComparison = org.apache.thrift.TBaseHelper.compareTo(
						this.body, other.body);
				if (lastComparison != 0) {
					return lastComparison;
				}
			}
			return 0;
		}

		public _Fields fieldForId(int fieldId) {
			return _Fields.findByThriftId(fieldId);
		}

		public void read(org.apache.thrift.protocol.TProtocol iprot)
				throws org.apache.thrift.TException {
			schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
		}

		public void write(org.apache.thrift.protocol.TProtocol oprot)
				throws org.apache.thrift.TException {
			schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder("putFile_args(");
			boolean first = true;

			sb.append("fileId:");
			sb.append(this.fileId);
			first = false;
			if (!first)
				sb.append(", ");
			sb.append("body:");
			if (this.body == null) {
				sb.append("null");
			} else {
				sb.append(this.body);
			}
			first = false;
			sb.append(")");
			return sb.toString();
		}

		public void validate() throws org.apache.thrift.TException {
			// check for required fields
			// check for sub-struct validity
		}

		private void writeObject(java.io.ObjectOutputStream out)
				throws java.io.IOException {
			try {
				write(new org.apache.thrift.protocol.TCompactProtocol(
						new org.apache.thrift.transport.TIOStreamTransport(out)));
			} catch (org.apache.thrift.TException te) {
				throw new java.io.IOException(te);
			}
		}

		private void readObject(java.io.ObjectInputStream in)
				throws java.io.IOException, ClassNotFoundException {
			try {
				// it doesn't seem like you should have to do this, but java
				// serialization is wacky, and doesn't call the default
				// constructor.
				__isset_bitfield = 0;
				read(new org.apache.thrift.protocol.TCompactProtocol(
						new org.apache.thrift.transport.TIOStreamTransport(in)));
			} catch (org.apache.thrift.TException te) {
				throw new java.io.IOException(te);
			}
		}

		private static class putFile_argsStandardSchemeFactory implements
				SchemeFactory {
			public putFile_argsStandardScheme getScheme() {
				return new putFile_argsStandardScheme();
			}
		}

		private static class putFile_argsStandardScheme extends
				StandardScheme<putFile_args> {

			public void read(org.apache.thrift.protocol.TProtocol iprot,
					putFile_args struct) throws org.apache.thrift.TException {
				org.apache.thrift.protocol.TField schemeField;
				iprot.readStructBegin();
				while (true) {
					schemeField = iprot.readFieldBegin();
					if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
						break;
					}
					switch (schemeField.id) {
					case 1: // FILE_ID
						if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
							struct.fileId = iprot.readI32();
							struct.setFileIdIsSet(true);
						} else {
							org.apache.thrift.protocol.TProtocolUtil.skip(
									iprot, schemeField.type);
						}
						break;
					case 2: // BODY
						if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
							{
								org.apache.thrift.protocol.TList _list0 = iprot
										.readListBegin();
								struct.body = new ArrayList<Byte>(_list0.size);
								for (int _i1 = 0; _i1 < _list0.size; ++_i1) {
									byte _elem2;
									_elem2 = iprot.readByte();
									struct.body.add(_elem2);
								}
								iprot.readListEnd();
							}
							struct.setBodyIsSet(true);
						} else {
							org.apache.thrift.protocol.TProtocolUtil.skip(
									iprot, schemeField.type);
						}
						break;
					default:
						org.apache.thrift.protocol.TProtocolUtil.skip(iprot,
								schemeField.type);
					}
					iprot.readFieldEnd();
				}
				iprot.readStructEnd();

				// check for required fields of primitive type, which can't be
				// checked in the validate method
				struct.validate();
			}

			public void write(org.apache.thrift.protocol.TProtocol oprot,
					putFile_args struct) throws org.apache.thrift.TException {
				struct.validate();

				oprot.writeStructBegin(STRUCT_DESC);
				oprot.writeFieldBegin(FILE_ID_FIELD_DESC);
				oprot.writeI32(struct.fileId);
				oprot.writeFieldEnd();
				if (struct.body != null) {
					oprot.writeFieldBegin(BODY_FIELD_DESC);
					{
						oprot.writeListBegin(new org.apache.thrift.protocol.TList(
								org.apache.thrift.protocol.TType.BYTE,
								struct.body.size()));
						for (byte _iter3 : struct.body) {
							oprot.writeByte(_iter3);
						}
						oprot.writeListEnd();
					}
					oprot.writeFieldEnd();
				}
				oprot.writeFieldStop();
				oprot.writeStructEnd();
			}

		}

		private static class putFile_argsTupleSchemeFactory implements
				SchemeFactory {
			public putFile_argsTupleScheme getScheme() {
				return new putFile_argsTupleScheme();
			}
		}

		private static class putFile_argsTupleScheme extends
				TupleScheme<putFile_args> {

			@Override
			public void write(org.apache.thrift.protocol.TProtocol prot,
					putFile_args struct) throws org.apache.thrift.TException {
				TTupleProtocol oprot = (TTupleProtocol) prot;
				BitSet optionals = new BitSet();
				if (struct.isSetFileId()) {
					optionals.set(0);
				}
				if (struct.isSetBody()) {
					optionals.set(1);
				}
				oprot.writeBitSet(optionals, 2);
				if (struct.isSetFileId()) {
					oprot.writeI32(struct.fileId);
				}
				if (struct.isSetBody()) {
					{
						oprot.writeI32(struct.body.size());
						for (byte _iter4 : struct.body) {
							oprot.writeByte(_iter4);
						}
					}
				}
			}

			@Override
			public void read(org.apache.thrift.protocol.TProtocol prot,
					putFile_args struct) throws org.apache.thrift.TException {
				TTupleProtocol iprot = (TTupleProtocol) prot;
				BitSet incoming = iprot.readBitSet(2);
				if (incoming.get(0)) {
					struct.fileId = iprot.readI32();
					struct.setFileIdIsSet(true);
				}
				if (incoming.get(1)) {
					{
						org.apache.thrift.protocol.TList _list5 = new org.apache.thrift.protocol.TList(
								org.apache.thrift.protocol.TType.BYTE,
								iprot.readI32());
						struct.body = new ArrayList<Byte>(_list5.size);
						for (int _i6 = 0; _i6 < _list5.size; ++_i6) {
							byte _elem7;
							_elem7 = iprot.readByte();
							struct.body.add(_elem7);
						}
					}
					struct.setBodyIsSet(true);
				}
			}
		}

	}

	public static class putFile_result implements
			org.apache.thrift.TBase<putFile_result, putFile_result._Fields>,
			java.io.Serializable, Cloneable, Comparable<putFile_result> {
		private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct(
				"putFile_result");

		private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
		static {
			schemes.put(StandardScheme.class,
					new putFile_resultStandardSchemeFactory());
			schemes.put(TupleScheme.class,
					new putFile_resultTupleSchemeFactory());
		}

		/**
		 * The set of fields this struct contains, along with convenience
		 * methods for finding and manipulating them.
		 */
		public enum _Fields implements org.apache.thrift.TFieldIdEnum {
			;

			private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

			static {
				for (_Fields field : EnumSet.allOf(_Fields.class)) {
					byName.put(field.getFieldName(), field);
				}
			}

			/**
			 * Find the _Fields constant that matches fieldId, or null if its
			 * not found.
			 */
			public static _Fields findByThriftId(int fieldId) {
				switch (fieldId) {
				default:
					return null;
				}
			}

			/**
			 * Find the _Fields constant that matches fieldId, throwing an
			 * exception if it is not found.
			 */
			public static _Fields findByThriftIdOrThrow(int fieldId) {
				_Fields fields = findByThriftId(fieldId);
				if (fields == null)
					throw new IllegalArgumentException("Field " + fieldId
							+ " doesn't exist!");
				return fields;
			}

			/**
			 * Find the _Fields constant that matches name, or null if its not
			 * found.
			 */
			public static _Fields findByName(String name) {
				return byName.get(name);
			}

			private final short _thriftId;
			private final String _fieldName;

			_Fields(short thriftId, String fieldName) {
				_thriftId = thriftId;
				_fieldName = fieldName;
			}

			public short getThriftFieldId() {
				return _thriftId;
			}

			public String getFieldName() {
				return _fieldName;
			}
		}

		public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
		static {
			Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(
					_Fields.class);
			metaDataMap = Collections.unmodifiableMap(tmpMap);
			org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(
					putFile_result.class, metaDataMap);
		}

		public putFile_result() {
		}

		/**
		 * Performs a deep copy on <i>other</i>.
		 */
		public putFile_result(putFile_result other) {
		}

		public putFile_result deepCopy() {
			return new putFile_result(this);
		}

		@Override
		public void clear() {
		}

		public void setFieldValue(_Fields field, Object value) {
			switch (field) {
			}
		}

		public Object getFieldValue(_Fields field) {
			switch (field) {
			}
			throw new IllegalStateException();
		}

		/**
		 * Returns true if field corresponding to fieldID is set (has been
		 * assigned a value) and false otherwise
		 */
		public boolean isSet(_Fields field) {
			if (field == null) {
				throw new IllegalArgumentException();
			}

			switch (field) {
			}
			throw new IllegalStateException();
		}

		@Override
		public boolean equals(Object that) {
			if (that == null)
				return false;
			if (that instanceof putFile_result)
				return this.equals((putFile_result) that);
			return false;
		}

		public boolean equals(putFile_result that) {
			if (that == null)
				return false;

			return true;
		}

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public int compareTo(putFile_result other) {
			if (!getClass().equals(other.getClass())) {
				return getClass().getName().compareTo(
						other.getClass().getName());
			}

			int lastComparison = 0;

			return 0;
		}

		public _Fields fieldForId(int fieldId) {
			return _Fields.findByThriftId(fieldId);
		}

		public void read(org.apache.thrift.protocol.TProtocol iprot)
				throws org.apache.thrift.TException {
			schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
		}

		public void write(org.apache.thrift.protocol.TProtocol oprot)
				throws org.apache.thrift.TException {
			schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder("putFile_result(");
			boolean first = true;

			sb.append(")");
			return sb.toString();
		}

		public void validate() throws org.apache.thrift.TException {
			// check for required fields
			// check for sub-struct validity
		}

		private void writeObject(java.io.ObjectOutputStream out)
				throws java.io.IOException {
			try {
				write(new org.apache.thrift.protocol.TCompactProtocol(
						new org.apache.thrift.transport.TIOStreamTransport(out)));
			} catch (org.apache.thrift.TException te) {
				throw new java.io.IOException(te);
			}
		}

		private void readObject(java.io.ObjectInputStream in)
				throws java.io.IOException, ClassNotFoundException {
			try {
				read(new org.apache.thrift.protocol.TCompactProtocol(
						new org.apache.thrift.transport.TIOStreamTransport(in)));
			} catch (org.apache.thrift.TException te) {
				throw new java.io.IOException(te);
			}
		}

		private static class putFile_resultStandardSchemeFactory implements
				SchemeFactory {
			public putFile_resultStandardScheme getScheme() {
				return new putFile_resultStandardScheme();
			}
		}

		private static class putFile_resultStandardScheme extends
				StandardScheme<putFile_result> {

			public void read(org.apache.thrift.protocol.TProtocol iprot,
					putFile_result struct) throws org.apache.thrift.TException {
				org.apache.thrift.protocol.TField schemeField;
				iprot.readStructBegin();
				while (true) {
					schemeField = iprot.readFieldBegin();
					if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
						break;
					}
					switch (schemeField.id) {
					default:
						org.apache.thrift.protocol.TProtocolUtil.skip(iprot,
								schemeField.type);
					}
					iprot.readFieldEnd();
				}
				iprot.readStructEnd();

				// check for required fields of primitive type, which can't be
				// checked in the validate method
				struct.validate();
			}

			public void write(org.apache.thrift.protocol.TProtocol oprot,
					putFile_result struct) throws org.apache.thrift.TException {
				struct.validate();

				oprot.writeStructBegin(STRUCT_DESC);
				oprot.writeFieldStop();
				oprot.writeStructEnd();
			}

		}

		private static class putFile_resultTupleSchemeFactory implements
				SchemeFactory {
			public putFile_resultTupleScheme getScheme() {
				return new putFile_resultTupleScheme();
			}
		}

		private static class putFile_resultTupleScheme extends
				TupleScheme<putFile_result> {

			@Override
			public void write(org.apache.thrift.protocol.TProtocol prot,
					putFile_result struct) throws org.apache.thrift.TException {
				TTupleProtocol oprot = (TTupleProtocol) prot;
			}

			@Override
			public void read(org.apache.thrift.protocol.TProtocol prot,
					putFile_result struct) throws org.apache.thrift.TException {
				TTupleProtocol iprot = (TTupleProtocol) prot;
			}
		}

	}

	public static class getFile_args implements
			org.apache.thrift.TBase<getFile_args, getFile_args._Fields>,
			java.io.Serializable, Cloneable, Comparable<getFile_args> {
		private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct(
				"getFile_args");

		private static final org.apache.thrift.protocol.TField FILE_ID_FIELD_DESC = new org.apache.thrift.protocol.TField(
				"fileId", org.apache.thrift.protocol.TType.I32, (short) 1);

		private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
		static {
			schemes.put(StandardScheme.class,
					new getFile_argsStandardSchemeFactory());
			schemes.put(TupleScheme.class, new getFile_argsTupleSchemeFactory());
		}

		public int fileId; // required

		/**
		 * The set of fields this struct contains, along with convenience
		 * methods for finding and manipulating them.
		 */
		public enum _Fields implements org.apache.thrift.TFieldIdEnum {
			FILE_ID((short) 1, "fileId");

			private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

			static {
				for (_Fields field : EnumSet.allOf(_Fields.class)) {
					byName.put(field.getFieldName(), field);
				}
			}

			/**
			 * Find the _Fields constant that matches fieldId, or null if its
			 * not found.
			 */
			public static _Fields findByThriftId(int fieldId) {
				switch (fieldId) {
				case 1: // FILE_ID
					return FILE_ID;
				default:
					return null;
				}
			}

			/**
			 * Find the _Fields constant that matches fieldId, throwing an
			 * exception if it is not found.
			 */
			public static _Fields findByThriftIdOrThrow(int fieldId) {
				_Fields fields = findByThriftId(fieldId);
				if (fields == null)
					throw new IllegalArgumentException("Field " + fieldId
							+ " doesn't exist!");
				return fields;
			}

			/**
			 * Find the _Fields constant that matches name, or null if its not
			 * found.
			 */
			public static _Fields findByName(String name) {
				return byName.get(name);
			}

			private final short _thriftId;
			private final String _fieldName;

			_Fields(short thriftId, String fieldName) {
				_thriftId = thriftId;
				_fieldName = fieldName;
			}

			public short getThriftFieldId() {
				return _thriftId;
			}

			public String getFieldName() {
				return _fieldName;
			}
		}

		// isset id assignments
		private static final int __FILEID_ISSET_ID = 0;
		private byte __isset_bitfield = 0;
		public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
		static {
			Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(
					_Fields.class);
			tmpMap.put(
					_Fields.FILE_ID,
					new org.apache.thrift.meta_data.FieldMetaData(
							"fileId",
							org.apache.thrift.TFieldRequirementType.DEFAULT,
							new org.apache.thrift.meta_data.FieldValueMetaData(
									org.apache.thrift.protocol.TType.I32, "int")));
			metaDataMap = Collections.unmodifiableMap(tmpMap);
			org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(
					getFile_args.class, metaDataMap);
		}

		public getFile_args() {
		}

		public getFile_args(int fileId) {
			this();
			this.fileId = fileId;
			setFileIdIsSet(true);
		}

		/**
		 * Performs a deep copy on <i>other</i>.
		 */
		public getFile_args(getFile_args other) {
			__isset_bitfield = other.__isset_bitfield;
			this.fileId = other.fileId;
		}

		public getFile_args deepCopy() {
			return new getFile_args(this);
		}

		@Override
		public void clear() {
			setFileIdIsSet(false);
			this.fileId = 0;
		}

		public int getFileId() {
			return this.fileId;
		}

		public getFile_args setFileId(int fileId) {
			this.fileId = fileId;
			setFileIdIsSet(true);
			return this;
		}

		public void unsetFileId() {
			__isset_bitfield = EncodingUtils.clearBit(__isset_bitfield,
					__FILEID_ISSET_ID);
		}

		/**
		 * Returns true if field fileId is set (has been assigned a value) and
		 * false otherwise
		 */
		public boolean isSetFileId() {
			return EncodingUtils.testBit(__isset_bitfield, __FILEID_ISSET_ID);
		}

		public void setFileIdIsSet(boolean value) {
			__isset_bitfield = EncodingUtils.setBit(__isset_bitfield,
					__FILEID_ISSET_ID, value);
		}

		public void setFieldValue(_Fields field, Object value) {
			switch (field) {
			case FILE_ID:
				if (value == null) {
					unsetFileId();
				} else {
					setFileId((Integer) value);
				}
				break;

			}
		}

		public Object getFieldValue(_Fields field) {
			switch (field) {
			case FILE_ID:
				return Integer.valueOf(getFileId());

			}
			throw new IllegalStateException();
		}

		/**
		 * Returns true if field corresponding to fieldID is set (has been
		 * assigned a value) and false otherwise
		 */
		public boolean isSet(_Fields field) {
			if (field == null) {
				throw new IllegalArgumentException();
			}

			switch (field) {
			case FILE_ID:
				return isSetFileId();
			}
			throw new IllegalStateException();
		}

		@Override
		public boolean equals(Object that) {
			if (that == null)
				return false;
			if (that instanceof getFile_args)
				return this.equals((getFile_args) that);
			return false;
		}

		public boolean equals(getFile_args that) {
			if (that == null)
				return false;

			boolean this_present_fileId = true;
			boolean that_present_fileId = true;
			if (this_present_fileId || that_present_fileId) {
				if (!(this_present_fileId && that_present_fileId))
					return false;
				if (this.fileId != that.fileId)
					return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public int compareTo(getFile_args other) {
			if (!getClass().equals(other.getClass())) {
				return getClass().getName().compareTo(
						other.getClass().getName());
			}

			int lastComparison = 0;

			lastComparison = Boolean.valueOf(isSetFileId()).compareTo(
					other.isSetFileId());
			if (lastComparison != 0) {
				return lastComparison;
			}
			if (isSetFileId()) {
				lastComparison = org.apache.thrift.TBaseHelper.compareTo(
						this.fileId, other.fileId);
				if (lastComparison != 0) {
					return lastComparison;
				}
			}
			return 0;
		}

		public _Fields fieldForId(int fieldId) {
			return _Fields.findByThriftId(fieldId);
		}

		public void read(org.apache.thrift.protocol.TProtocol iprot)
				throws org.apache.thrift.TException {
			schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
		}

		public void write(org.apache.thrift.protocol.TProtocol oprot)
				throws org.apache.thrift.TException {
			schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder("getFile_args(");
			boolean first = true;

			sb.append("fileId:");
			sb.append(this.fileId);
			first = false;
			sb.append(")");
			return sb.toString();
		}

		public void validate() throws org.apache.thrift.TException {
			// check for required fields
			// check for sub-struct validity
		}

		private void writeObject(java.io.ObjectOutputStream out)
				throws java.io.IOException {
			try {
				write(new org.apache.thrift.protocol.TCompactProtocol(
						new org.apache.thrift.transport.TIOStreamTransport(out)));
			} catch (org.apache.thrift.TException te) {
				throw new java.io.IOException(te);
			}
		}

		private void readObject(java.io.ObjectInputStream in)
				throws java.io.IOException, ClassNotFoundException {
			try {
				// it doesn't seem like you should have to do this, but java
				// serialization is wacky, and doesn't call the default
				// constructor.
				__isset_bitfield = 0;
				read(new org.apache.thrift.protocol.TCompactProtocol(
						new org.apache.thrift.transport.TIOStreamTransport(in)));
			} catch (org.apache.thrift.TException te) {
				throw new java.io.IOException(te);
			}
		}

		private static class getFile_argsStandardSchemeFactory implements
				SchemeFactory {
			public getFile_argsStandardScheme getScheme() {
				return new getFile_argsStandardScheme();
			}
		}

		private static class getFile_argsStandardScheme extends
				StandardScheme<getFile_args> {

			public void read(org.apache.thrift.protocol.TProtocol iprot,
					getFile_args struct) throws org.apache.thrift.TException {
				org.apache.thrift.protocol.TField schemeField;
				iprot.readStructBegin();
				while (true) {
					schemeField = iprot.readFieldBegin();
					if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
						break;
					}
					switch (schemeField.id) {
					case 1: // FILE_ID
						if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
							struct.fileId = iprot.readI32();
							struct.setFileIdIsSet(true);
						} else {
							org.apache.thrift.protocol.TProtocolUtil.skip(
									iprot, schemeField.type);
						}
						break;
					default:
						org.apache.thrift.protocol.TProtocolUtil.skip(iprot,
								schemeField.type);
					}
					iprot.readFieldEnd();
				}
				iprot.readStructEnd();

				// check for required fields of primitive type, which can't be
				// checked in the validate method
				struct.validate();
			}

			public void write(org.apache.thrift.protocol.TProtocol oprot,
					getFile_args struct) throws org.apache.thrift.TException {
				struct.validate();

				oprot.writeStructBegin(STRUCT_DESC);
				oprot.writeFieldBegin(FILE_ID_FIELD_DESC);
				oprot.writeI32(struct.fileId);
				oprot.writeFieldEnd();
				oprot.writeFieldStop();
				oprot.writeStructEnd();
			}

		}

		private static class getFile_argsTupleSchemeFactory implements
				SchemeFactory {
			public getFile_argsTupleScheme getScheme() {
				return new getFile_argsTupleScheme();
			}
		}

		private static class getFile_argsTupleScheme extends
				TupleScheme<getFile_args> {

			@Override
			public void write(org.apache.thrift.protocol.TProtocol prot,
					getFile_args struct) throws org.apache.thrift.TException {
				TTupleProtocol oprot = (TTupleProtocol) prot;
				BitSet optionals = new BitSet();
				if (struct.isSetFileId()) {
					optionals.set(0);
				}
				oprot.writeBitSet(optionals, 1);
				if (struct.isSetFileId()) {
					oprot.writeI32(struct.fileId);
				}
			}

			@Override
			public void read(org.apache.thrift.protocol.TProtocol prot,
					getFile_args struct) throws org.apache.thrift.TException {
				TTupleProtocol iprot = (TTupleProtocol) prot;
				BitSet incoming = iprot.readBitSet(1);
				if (incoming.get(0)) {
					struct.fileId = iprot.readI32();
					struct.setFileIdIsSet(true);
				}
			}
		}

	}

	public static class getFile_result implements
			org.apache.thrift.TBase<getFile_result, getFile_result._Fields>,
			java.io.Serializable, Cloneable, Comparable<getFile_result> {
		private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct(
				"getFile_result");

		private static final org.apache.thrift.protocol.TField SUCCESS_FIELD_DESC = new org.apache.thrift.protocol.TField(
				"success", org.apache.thrift.protocol.TType.LIST, (short) 0);

		private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
		static {
			schemes.put(StandardScheme.class,
					new getFile_resultStandardSchemeFactory());
			schemes.put(TupleScheme.class,
					new getFile_resultTupleSchemeFactory());
		}

		public List<Byte> success; // required

		/**
		 * The set of fields this struct contains, along with convenience
		 * methods for finding and manipulating them.
		 */
		public enum _Fields implements org.apache.thrift.TFieldIdEnum {
			SUCCESS((short) 0, "success");

			private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

			static {
				for (_Fields field : EnumSet.allOf(_Fields.class)) {
					byName.put(field.getFieldName(), field);
				}
			}

			/**
			 * Find the _Fields constant that matches fieldId, or null if its
			 * not found.
			 */
			public static _Fields findByThriftId(int fieldId) {
				switch (fieldId) {
				case 0: // SUCCESS
					return SUCCESS;
				default:
					return null;
				}
			}

			/**
			 * Find the _Fields constant that matches fieldId, throwing an
			 * exception if it is not found.
			 */
			public static _Fields findByThriftIdOrThrow(int fieldId) {
				_Fields fields = findByThriftId(fieldId);
				if (fields == null)
					throw new IllegalArgumentException("Field " + fieldId
							+ " doesn't exist!");
				return fields;
			}

			/**
			 * Find the _Fields constant that matches name, or null if its not
			 * found.
			 */
			public static _Fields findByName(String name) {
				return byName.get(name);
			}

			private final short _thriftId;
			private final String _fieldName;

			_Fields(short thriftId, String fieldName) {
				_thriftId = thriftId;
				_fieldName = fieldName;
			}

			public short getThriftFieldId() {
				return _thriftId;
			}

			public String getFieldName() {
				return _fieldName;
			}
		}

		// isset id assignments
		public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
		static {
			Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(
					_Fields.class);
			tmpMap.put(
					_Fields.SUCCESS,
					new org.apache.thrift.meta_data.FieldMetaData(
							"success",
							org.apache.thrift.TFieldRequirementType.DEFAULT,
							new org.apache.thrift.meta_data.ListMetaData(
									org.apache.thrift.protocol.TType.LIST,
									new org.apache.thrift.meta_data.FieldValueMetaData(
											org.apache.thrift.protocol.TType.BYTE))));
			metaDataMap = Collections.unmodifiableMap(tmpMap);
			org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(
					getFile_result.class, metaDataMap);
		}

		public getFile_result() {
		}

		public getFile_result(List<Byte> success) {
			this();
			this.success = success;
		}

		/**
		 * Performs a deep copy on <i>other</i>.
		 */
		public getFile_result(getFile_result other) {
			if (other.isSetSuccess()) {
				List<Byte> __this__success = new ArrayList<Byte>(other.success);
				this.success = __this__success;
			}
		}

		public getFile_result deepCopy() {
			return new getFile_result(this);
		}

		@Override
		public void clear() {
			this.success = null;
		}

		public int getSuccessSize() {
			return (this.success == null) ? 0 : this.success.size();
		}

		public java.util.Iterator<Byte> getSuccessIterator() {
			return (this.success == null) ? null : this.success.iterator();
		}

		public void addToSuccess(byte elem) {
			if (this.success == null) {
				this.success = new ArrayList<Byte>();
			}
			this.success.add(elem);
		}

		public List<Byte> getSuccess() {
			return this.success;
		}

		public getFile_result setSuccess(List<Byte> success) {
			this.success = success;
			return this;
		}

		public void unsetSuccess() {
			this.success = null;
		}

		/**
		 * Returns true if field success is set (has been assigned a value) and
		 * false otherwise
		 */
		public boolean isSetSuccess() {
			return this.success != null;
		}

		public void setSuccessIsSet(boolean value) {
			if (!value) {
				this.success = null;
			}
		}

		public void setFieldValue(_Fields field, Object value) {
			switch (field) {
			case SUCCESS:
				if (value == null) {
					unsetSuccess();
				} else {
					setSuccess((List<Byte>) value);
				}
				break;

			}
		}

		public Object getFieldValue(_Fields field) {
			switch (field) {
			case SUCCESS:
				return getSuccess();

			}
			throw new IllegalStateException();
		}

		/**
		 * Returns true if field corresponding to fieldID is set (has been
		 * assigned a value) and false otherwise
		 */
		public boolean isSet(_Fields field) {
			if (field == null) {
				throw new IllegalArgumentException();
			}

			switch (field) {
			case SUCCESS:
				return isSetSuccess();
			}
			throw new IllegalStateException();
		}

		@Override
		public boolean equals(Object that) {
			if (that == null)
				return false;
			if (that instanceof getFile_result)
				return this.equals((getFile_result) that);
			return false;
		}

		public boolean equals(getFile_result that) {
			if (that == null)
				return false;

			boolean this_present_success = true && this.isSetSuccess();
			boolean that_present_success = true && that.isSetSuccess();
			if (this_present_success || that_present_success) {
				if (!(this_present_success && that_present_success))
					return false;
				if (!this.success.equals(that.success))
					return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public int compareTo(getFile_result other) {
			if (!getClass().equals(other.getClass())) {
				return getClass().getName().compareTo(
						other.getClass().getName());
			}

			int lastComparison = 0;

			lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(
					other.isSetSuccess());
			if (lastComparison != 0) {
				return lastComparison;
			}
			if (isSetSuccess()) {
				lastComparison = org.apache.thrift.TBaseHelper.compareTo(
						this.success, other.success);
				if (lastComparison != 0) {
					return lastComparison;
				}
			}
			return 0;
		}

		public _Fields fieldForId(int fieldId) {
			return _Fields.findByThriftId(fieldId);
		}

		public void read(org.apache.thrift.protocol.TProtocol iprot)
				throws org.apache.thrift.TException {
			schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
		}

		public void write(org.apache.thrift.protocol.TProtocol oprot)
				throws org.apache.thrift.TException {
			schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder("getFile_result(");
			boolean first = true;

			sb.append("success:");
			if (this.success == null) {
				sb.append("null");
			} else {
				sb.append(this.success);
			}
			first = false;
			sb.append(")");
			return sb.toString();
		}

		public void validate() throws org.apache.thrift.TException {
			// check for required fields
			// check for sub-struct validity
		}

		private void writeObject(java.io.ObjectOutputStream out)
				throws java.io.IOException {
			try {
				write(new org.apache.thrift.protocol.TCompactProtocol(
						new org.apache.thrift.transport.TIOStreamTransport(out)));
			} catch (org.apache.thrift.TException te) {
				throw new java.io.IOException(te);
			}
		}

		private void readObject(java.io.ObjectInputStream in)
				throws java.io.IOException, ClassNotFoundException {
			try {
				read(new org.apache.thrift.protocol.TCompactProtocol(
						new org.apache.thrift.transport.TIOStreamTransport(in)));
			} catch (org.apache.thrift.TException te) {
				throw new java.io.IOException(te);
			}
		}

		private static class getFile_resultStandardSchemeFactory implements
				SchemeFactory {
			public getFile_resultStandardScheme getScheme() {
				return new getFile_resultStandardScheme();
			}
		}

		private static class getFile_resultStandardScheme extends
				StandardScheme<getFile_result> {

			public void read(org.apache.thrift.protocol.TProtocol iprot,
					getFile_result struct) throws org.apache.thrift.TException {
				org.apache.thrift.protocol.TField schemeField;
				iprot.readStructBegin();
				while (true) {
					schemeField = iprot.readFieldBegin();
					if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
						break;
					}
					switch (schemeField.id) {
					case 0: // SUCCESS
						if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
							{
								org.apache.thrift.protocol.TList _list8 = iprot
										.readListBegin();
								struct.success = new ArrayList<Byte>(
										_list8.size);
								for (int _i9 = 0; _i9 < _list8.size; ++_i9) {
									byte _elem10;
									_elem10 = iprot.readByte();
									struct.success.add(_elem10);
								}
								iprot.readListEnd();
							}
							struct.setSuccessIsSet(true);
						} else {
							org.apache.thrift.protocol.TProtocolUtil.skip(
									iprot, schemeField.type);
						}
						break;
					default:
						org.apache.thrift.protocol.TProtocolUtil.skip(iprot,
								schemeField.type);
					}
					iprot.readFieldEnd();
				}
				iprot.readStructEnd();

				// check for required fields of primitive type, which can't be
				// checked in the validate method
				struct.validate();
			}

			public void write(org.apache.thrift.protocol.TProtocol oprot,
					getFile_result struct) throws org.apache.thrift.TException {
				struct.validate();

				oprot.writeStructBegin(STRUCT_DESC);
				if (struct.success != null) {
					oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
					{
						oprot.writeListBegin(new org.apache.thrift.protocol.TList(
								org.apache.thrift.protocol.TType.BYTE,
								struct.success.size()));
						for (byte _iter11 : struct.success) {
							oprot.writeByte(_iter11);
						}
						oprot.writeListEnd();
					}
					oprot.writeFieldEnd();
				}
				oprot.writeFieldStop();
				oprot.writeStructEnd();
			}

		}

		private static class getFile_resultTupleSchemeFactory implements
				SchemeFactory {
			public getFile_resultTupleScheme getScheme() {
				return new getFile_resultTupleScheme();
			}
		}

		private static class getFile_resultTupleScheme extends
				TupleScheme<getFile_result> {

			@Override
			public void write(org.apache.thrift.protocol.TProtocol prot,
					getFile_result struct) throws org.apache.thrift.TException {
				TTupleProtocol oprot = (TTupleProtocol) prot;
				BitSet optionals = new BitSet();
				if (struct.isSetSuccess()) {
					optionals.set(0);
				}
				oprot.writeBitSet(optionals, 1);
				if (struct.isSetSuccess()) {
					{
						oprot.writeI32(struct.success.size());
						for (byte _iter12 : struct.success) {
							oprot.writeByte(_iter12);
						}
					}
				}
			}

			@Override
			public void read(org.apache.thrift.protocol.TProtocol prot,
					getFile_result struct) throws org.apache.thrift.TException {
				TTupleProtocol iprot = (TTupleProtocol) prot;
				BitSet incoming = iprot.readBitSet(1);
				if (incoming.get(0)) {
					{
						org.apache.thrift.protocol.TList _list13 = new org.apache.thrift.protocol.TList(
								org.apache.thrift.protocol.TType.BYTE,
								iprot.readI32());
						struct.success = new ArrayList<Byte>(_list13.size);
						for (int _i14 = 0; _i14 < _list13.size; ++_i14) {
							byte _elem15;
							_elem15 = iprot.readByte();
							struct.success.add(_elem15);
						}
					}
					struct.setSuccessIsSet(true);
				}
			}
		}

	}

}
