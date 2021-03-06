package yn4_jjc7.client.model.chatroom;

import java.awt.Component;
import java.awt.Window;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JFrame;

import provided.datapacket.ADataPacketAlgoCmd;
import provided.datapacket.DataPacket;
import provided.datapacket.DataPacketAlgo;
import provided.mixedData.IMixedDataDictionary;
import provided.mixedData.MixedDataDictionary;
import yn4_jjc7.client.model.message.chat.*;
import yn4_jjc7.client.model.message.chat.unknown.DonaldAlgo;
import yn4_jjc7.client.model.message.chat.unknown.DonaldMessage;
import yn4_jjc7.client.model.message.chat.unknown.MapAlgo;
import yn4_jjc7.client.model.message.chat.unknown.MapMessage;
import common.ICmd2ModelAdapter;
import common.chatroom.IChatroomAdapter;
import common.chatroom.IChatroomID;
import common.message.IErrorMessage;
import common.message.INullMessage;
import common.message.NullMessage;
import common.message.chat.*;
import common.user.IUser;

/**
 * Model for a chatroom instance.
 */
public class Chatroom {

	/** The identifier for this chatroom */
	private IChatroomID chatroomId;

	/** The adapter to the view */
	private IChatroomToViewAdapter view;

	/** The adapter for the local user */
	private IUser thisUser;

	/** The list of adapters to all users in the chatroom (includes thisUser) */
	private List<IChatroomAdapter> userChatroomAdapters;

	/** The mixed data dictionary for use by unknown messages sent to this chatrooom */
	private MixedDataDictionary data = new MixedDataDictionary();

	/** The adapter to this chatroom that will be sent to other users to communicate with us */
	private IChatroomAdapter thisAdapter;

	/** The adapter to the main model that manages this chatroom */
	private IChatroomToModelAdapter model;

	/**
	 * The remote that will be encapsulated in the thisUser adapter to help remote users communicate with this chatroom
	 * instance
	 */
	private IChatroomToChatroomRemote thisRemote = new IChatroomToChatroomRemote() {
		@Override
		public DataPacket<? extends IChatMessage> sendChatroomMessage(
				DataPacket<? extends IChatMessage> message, IChatroomAdapter sendingAdapter)
				throws RemoteException {
			System.out.println("Processed a message");
			return message.execute(chatVisitor, sendingAdapter);
		}
	};

	/** TODO The general cmd2ModelAdapter for our chatroom implementation */
	private ICmd2ModelAdapter cmd2ModelAdapter = new ICmd2ModelAdapter() {

		@Override
		public void addComponent(Component component, String label) {
			view.addComponent(component);
		}

		@Override
		public Window addComponentAsWindow(Component component, String label) {
			JFrame frame = new JFrame(label);
			frame.getContentPane().add(component);
			frame.pack();
			frame.setVisible(true);
			return frame;
		}

		@Override
		public void append(String text) {
			view.displayMessage(text);
		}

		@Override
		public IMixedDataDictionary getMixedDataDictionary() {
			return data;
		}

		@Override
		public IChatroomAdapter getChatroomAdapter() {
			return thisAdapter;
		}

	};

	/** TODO Comment */
	private DataPacketAlgo<Void, IChatroomAdapter> chatReturnVisitor = new DataPacketAlgo<Void, IChatroomAdapter>(

			/* ==================== Default case ==================== */
			new ADataPacketAlgoCmd<Void, Object, IChatroomAdapter>() {

				private static final long serialVersionUID = 669374218821218396L;

				@Override
				public Void apply(Class<?> index, DataPacket<Object> host, IChatroomAdapter... params) {
					host.execute(chatVisitor, params);
					return null;
				}

				@Override
				public void setCmd2ModelAdpt(ICmd2ModelAdapter cmd2ModelAdpt) {
				}

			}) {

		private static final long serialVersionUID = 669374218821218396L;

		/* Add commands to visitor */
		{

			/* ==================== Null Message case ==================== */
			setCmd(INullMessage.class,
					new ADataPacketAlgoCmd<Void, Object, IChatroomAdapter>() {

						private static final long serialVersionUID = -1164505654950026201L;

						@Override
						public Void apply(Class<?> index, DataPacket<Object> host, IChatroomAdapter... params) {
							// Do nothing, don't have to send another message
							return null;
						}

						@Override
						public void setCmd2ModelAdpt(ICmd2ModelAdapter cmd2ModelAdpt) {
						}

					});

			/* ==================== Error Message case ==================== */
			setCmd(IErrorMessage.class,
					new ADataPacketAlgoCmd<Void, Object, IChatroomAdapter>() {

						private static final long serialVersionUID = -3105909347628384481L;

						@Override
						public Void apply(Class<?> index, DataPacket<Object> host, IChatroomAdapter... params) {
							IErrorMessage errorMessage = (IErrorMessage) host.getData();
							System.err.println("Got ErrorMessage: " + errorMessage.getCause().getMessage());
							return null; // Don't have to send another message
						}

						@Override
						public void setCmd2ModelAdpt(ICmd2ModelAdapter cmd2ModelAdpt) {
						}

					});

		}
	};

	/**
	 * The visitor that will be used by this chatroom to process DataPacket objects that contain IChatMessage objects
	 */
	private DataPacketAlgo<DataPacket<? extends IChatMessage>, IChatroomAdapter> chatVisitor = new DataPacketAlgo<DataPacket<? extends IChatMessage>, IChatroomAdapter>(
			new ADataPacketAlgoCmd<DataPacket<? extends IChatMessage>, Object, IChatroomAdapter>() {

				/* ==================== Unknown message case ==================== */

				private static final long serialVersionUID = 7541508735919507513L;

				@Override
				public DataPacket<? extends IChatMessage> apply(Class<?> index,
						DataPacket<Object> host, IChatroomAdapter... params) {
					IChatroomAdapter sender = params[0];

					try {
						// message with command
						DataPacket<? extends IChatMessage> response =
								sender.sendChatroomMessage(new DataPacket<IRequestCmdMessage>(IRequestCmdMessage.class,
										new RequestCmdMessage(index)), thisAdapter); 

						response.execute(chatReturnVisitor, sender); // install command
						return host.execute(chatVisitor, params); // the original command 
					} catch (RemoteException e) {

					}
					return new DataPacket<INullMessage>(INullMessage.class, NullMessage.SINGLETON);
				}

				@Override
				public void setCmd2ModelAdpt(ICmd2ModelAdapter cmd2ModelAdpt) {
				}

			}) {

		private static final long serialVersionUID = -7462140994737597017L;

		{

			/* ==================== Null message case==================== */
			setCmd(INullMessage.class,
					new ADataPacketAlgoCmd<DataPacket<? extends IChatMessage>, Object, IChatroomAdapter>() {

						private static final long serialVersionUID = 5861230867628406578L;

						@Override
						public DataPacket<? extends IChatMessage> apply(Class<?> index,
								DataPacket<Object> host, IChatroomAdapter... params) {
							return new DataPacket<INullMessage>(INullMessage.class, NullMessage.SINGLETON);
						}

						@Override
						public void setCmd2ModelAdpt(ICmd2ModelAdapter cmd2ModelAdpt) {
						}

					});

			/* ==================== Error message case ==================== */
			setCmd(IErrorMessage.class,
					new ADataPacketAlgoCmd<DataPacket<? extends IChatMessage>, Object, IChatroomAdapter>() {

						private static final long serialVersionUID = 4826965457258701801L;

						@Override
						public DataPacket<? extends IChatMessage> apply(Class<?> index,
								DataPacket<Object> host, IChatroomAdapter... params) {
							IErrorMessage error = (IErrorMessage) host;
							System.err.println("Got ErrorMessage: " + error.getCause().getMessage());
							error.getCause().printStackTrace();
							return new DataPacket<INullMessage>(INullMessage.class, NullMessage.SINGLETON);
						}

						@Override
						public void setCmd2ModelAdpt(ICmd2ModelAdapter cmd2ModelAdpt) {
						}

					});

			/* ==================== Ping case ==================== */
			setCmd(
					IPingMessage.class,
					new ADataPacketAlgoCmd<DataPacket<? extends IChatMessage>, Object, IChatroomAdapter>() {

						private static final long serialVersionUID = 914807586642021802L;

						@Override
						public DataPacket<? extends IChatMessage> apply(Class<?> index,
								DataPacket<Object> host, IChatroomAdapter... params) {
							return new DataPacket<INullMessage>(INullMessage.class, NullMessage.SINGLETON);
						}

						@Override
						public void setCmd2ModelAdpt(ICmd2ModelAdapter cmd2ModelAdpt) {
						}

					});

			/* ==================== Request Command case ==================== */
			setCmd(IRequestCmdMessage.class,
					new ADataPacketAlgoCmd<DataPacket<? extends IChatMessage>, Object, IChatroomAdapter>() {

						private static final long serialVersionUID = 3295103158269328527L;

						/**
						 * Request command case: called when a user needs a command for a message they did not know how
						 * to process
						 */
						@Override
						public DataPacket<? extends IChatMessage> apply(Class<?> index,
								DataPacket<Object> host, IChatroomAdapter... params) {
							IRequestCmdMessage requestCmdMsg = (IRequestCmdMessage) host.getData();

							// A new SendCmdMessage containing our command for a message unknown to the other user
							ISendCmdMessage messageToSend = new SendCmdMessage(
									requestCmdMsg.getMessageID(),
									(ADataPacketAlgoCmd<DataPacket<? extends IChatMessage>, Class<?>, IChatroomAdapter>)
									chatVisitor.getCmd(requestCmdMsg.getMessageID()));

							return new DataPacket<ISendCmdMessage>(ISendCmdMessage.class, messageToSend);
						}

						@Override
						public void setCmd2ModelAdpt(ICmd2ModelAdapter cmd2ModelAdpt) {
						}

					});

			/* ==================== Send command case ==================== */
			setCmd(ISendCmdMessage.class,
					new ADataPacketAlgoCmd<DataPacket<? extends IChatMessage>, Object, IChatroomAdapter>() {

						private static final long serialVersionUID = -1746480363664278026L;

						/**
						 * Send command case: called when we received the command to handle a message that we did not
						 * know how to process
						 */
						@Override
						public DataPacket<? extends IChatMessage> apply(Class<?> index, DataPacket<Object> host,
								IChatroomAdapter... params) {
							ISendCmdMessage sendCmdMsg = (ISendCmdMessage) host.getData();

							// Set the command
							chatVisitor.setCmd(sendCmdMsg.getCmdID(), sendCmdMsg.getCmd());
							// Set the cmd2ModelAdapter for the received command
							sendCmdMsg.getCmd().setCmd2ModelAdpt(cmd2ModelAdapter);
							
							return new DataPacket<INullMessage>(INullMessage.class, NullMessage.SINGLETON);
						}

						@Override
						public void setCmd2ModelAdpt(ICmd2ModelAdapter cmd2ModelAdpt) {
						}

					});

			/* ==================== Text message case ==================== */
			setCmd(ITextMessage.class,
					new ADataPacketAlgoCmd<DataPacket<? extends IChatMessage>, Object, IChatroomAdapter>() {

						private static final long serialVersionUID = 469976173113304452L;

						/**
						 * Text message case: called when we receive a chat message from another user
						 */
						@Override
						public DataPacket<? extends IChatMessage> apply(Class<?> index,
								DataPacket<Object> host, IChatroomAdapter... params) {
							ITextMessage txtMessage = (ITextMessage) host.getData();
							IChatroomAdapter sendingRoom = (IChatroomAdapter) params[0];

							view.displayMessage(String.format("%s (%s): %s",
									sendingRoom.getUser().toString(),
									new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()), // time
									txtMessage.getText()));

							return new DataPacket<INullMessage>(INullMessage.class, NullMessage.SINGLETON);
						}

						@Override
						public void setCmd2ModelAdpt(ICmd2ModelAdapter cmd2ModelAdpt) {
						}

					});

			/* ==================== Join chatroom case ==================== */
			setCmd(IJoinChatroomMessage.class,
					new ADataPacketAlgoCmd<DataPacket<? extends IChatMessage>, Object, IChatroomAdapter>() {

						private static final long serialVersionUID = 4533714929612084218L;

						/**
						 * Join chatroom case: called when a new user joined this chatroom
						 */
						@Override
						public DataPacket<? extends IChatMessage> apply(Class<?> index,
								DataPacket<Object> host, IChatroomAdapter... params) {
							IJoinChatroomMessage joinMessage = (IJoinChatroomMessage) host.getData();
							IChatroomAdapter sendingRoom = params[0];

							userChatroomAdapters.add(joinMessage.getAdapter());
							view.displayMessage(String.format("%s joined the room.", sendingRoom.getUser().toString()));

							return new DataPacket<INullMessage>(INullMessage.class, NullMessage.SINGLETON);
						}

						@Override
						public void setCmd2ModelAdpt(ICmd2ModelAdapter cmd2ModelAdpt) {
						}

					});

			/* ==================== Leave message case ==================== */
			setCmd(ILeaveMessage.class,
					new ADataPacketAlgoCmd<DataPacket<? extends IChatMessage>, Object, IChatroomAdapter>() {

						private static final long serialVersionUID = 3390382639006865241L;

						/* Leave message case: called when a user in this chatroom leaves the room */

						@Override
						public DataPacket<? extends IChatMessage> apply(Class<?> index,
								DataPacket<Object> host, IChatroomAdapter... params) {
							ILeaveMessage leaveMessage = (ILeaveMessage) host.getData();

							System.out.println("Existing adapters");
							for (IChatroomAdapter a : userChatroomAdapters) {
								System.out.println("existing" + a.getChatroomID() + ", " + a.getUser()
										+ Integer.toString(a.hashCode()));
							}
							System.out.println("leaveMesssage" + leaveMessage.getAdapter().getChatroomID()
									+ ", " + leaveMessage.getAdapter().getUser()
									+ Integer.toString(leaveMessage.getAdapter().hashCode()));
							userChatroomAdapters.remove(leaveMessage.getAdapter());
							System.out.println("-----------------------");
							System.out.println("user list: " + userChatroomAdapters);
							view.displayMessage(String.format("%s left the room.",
									leaveMessage.getAdapter().getUser().toString()));

							return new DataPacket<INullMessage>(INullMessage.class, NullMessage.SINGLETON);
						}

						@Override
						public void setCmd2ModelAdpt(ICmd2ModelAdapter cmd2ModelAdpt) {
						}

					});
          
			/*
			 * The unknown message case that will be sent to other users that do not know how to process
			 */
			setCmd(DonaldMessage.class, new DonaldAlgo(cmd2ModelAdapter));

			setCmd(MapMessage.class, new MapAlgo(cmd2ModelAdapter));
		}
	};

	/**
	 * Constructs a chatroom instance from an existing unique chatroom ID and list of user adapters.
	 * 
	 * @param chatroomId the unique identifier for this chatroom instance
	 * @param thisUser the local user's adapter
	 * @param view the adapter to the chatroom's view
	 * @param model the adapter to the model that manages this chatroom
	 * @param userStubs the list of adapters to other users in this chatroom
	 */
	public Chatroom(IChatroomID chatroomId, IUser thisUser, IChatroomToViewAdapter view,
			IChatroomToModelAdapter model, List<IChatroomAdapter> userStubs) {
		this.thisUser = thisUser;
		this.view = view;
		this.userChatroomAdapters = userStubs;
		this.chatroomId = chatroomId;
		this.model = model;

		try {
			IChatroomToChatroomRemote thisChatroomStub =
					(IChatroomToChatroomRemote) UnicastRemoteObject.exportObject(thisRemote, 2101);
			this.thisAdapter = new ChatroomAdapter(thisUser, chatroomId, thisChatroomStub);
			this.userChatroomAdapters.add(this.thisAdapter);

			IJoinChatroomMessage joinMessage = new JoinChatroomMessage(this.thisAdapter);

			for (IChatroomAdapter existingUser : this.userChatroomAdapters) {
				if (existingUser.getUser().equals(this.thisUser)) {
					continue;
				}

				new Thread(() -> {
					try {
						existingUser.sendChatroomMessage(new DataPacket<IJoinChatroomMessage>(
								IJoinChatroomMessage.class, joinMessage), this.thisAdapter);
					} catch (RemoteException e) {
						removeBrokenAdapter(existingUser);
					}
				}).start();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * Constructs a new chatroom instance with a new unique chatroom ID and an empty list of users except for the local
	 * user.
	 * 
	 * @param name the name for this chatroom
	 * @param thisUser the local user's adapter
	 * @param view the adapter to the chatroom's view
	 * @param model the adapter to the model that manages this chatroom
	 */
	public Chatroom(String name, IUser thisUser, IChatroomToViewAdapter view, IChatroomToModelAdapter model) {
		this(new ChatroomID(thisUser.getIPAddress(), name),
				thisUser, view, model,
				new CopyOnWriteArrayList<IChatroomAdapter>());
	}

	/**
	 * @return the unique identifier for this chatroom
	 */
	public IChatroomID getChatroomID() {
		return this.chatroomId;
	}

	/**
	 * @return the name of this chatroom
	 */
	public String getName() {
		return this.chatroomId.toString();
	}

	/**
	 * Returns the list of user adapters of the members of this chatroom.
	 * 
	 * @return the list of user adapters of the members of this chatroom
	 */
	public List<IChatroomAdapter> getUserStubs() {
		return userChatroomAdapters;
	}

	/**
	 * Installs the specified adapter as this chatroom's view adapter.
	 * 
	 * @param view the view adapter to install
	 */
	public void installViewAdapter(IChatroomToViewAdapter view) {
		this.view = view;
	}

	/**
	 * Asynchronously sends leave messages to all connected users in the chatroom and removes the chatroom view from the
	 * GUI via an adapter. After all messages have been sent, the reference to this chatroom is removed from the model
	 * that manages this chatroom via an adapter.
	 */
	public void leaveRoom() {
		LeaveMessage leaveMessage = new LeaveMessage(this.thisAdapter);

		userChatroomAdapters.remove(thisAdapter);

		view.removeChatroom();

		for (IChatroomAdapter room : userChatroomAdapters) {
			new Thread(() -> {
				try {
					room.sendChatroomMessage(new DataPacket<ILeaveMessage>(ILeaveMessage.class, leaveMessage),
							thisAdapter);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}).start();
		}

		model.removeChatroom(this);
	}

	/**
	 * Removes the specified chatroom adapter from the list of adapters and displays an informative message to the view
	 * informing that the chatroom has lost connection with the owner of that adapter.
	 * 
	 * @param adapter the broken adapter to remove
	 */
	private void removeBrokenAdapter(IChatroomAdapter adapter) { // TODO change to synchronized
		view.displayMessage("Lost connection with " + adapter.getUser());
		userChatroomAdapters.remove(adapter);
	}

	/**
	 * Asynchronously sends the specified message to all chatrooms in the <code>userChatroomAdapters</code> list. If a
	 * message fails to send to any recipient, then the adapter for that recipient is declared broken and handled by the
	 * <code>removeBrokenAdapter</code> method.
	 * 
	 * @param message the message to send
	 */
	public void sendMessage(String message) {
		TextMessage txtMessage = new TextMessage(message);

		for (IChatroomAdapter room : userChatroomAdapters) {
			System.out.println("Sending message to " + room.getUser().toString());
			new Thread(() -> {
				try {
					room.sendChatroomMessage(new DataPacket<ITextMessage>(ITextMessage.class, txtMessage),
							thisAdapter);
				} catch (RemoteException e) {
					removeBrokenAdapter(room);
				}
			}).start();
		}
	}

	/**
	 * Asynchronously sends the unknown message type to all chatrooms in the <code>userChatroomAdapters</code> list. If
	 * a message fails to send to any recipient, then the adapter for that recipient is declared broken and handled by
	 * the <code>removeBrokenAdapter</code> method.
	 */
	public void sendDonald() {
		List<IChatroomAdapter> removeLater = new ArrayList<IChatroomAdapter>();

		for (IChatroomAdapter room : userChatroomAdapters) {
			new Thread(() -> {
				try {
					room.sendChatroomMessage(new DataPacket<DonaldMessage>(DonaldMessage.class,
							new DonaldMessage()), this.thisAdapter);
				} catch (RemoteException e) {
					removeLater.add(room); // TODO change to removeBrokenAdapter(..)
				}
			}).start();
		}
	}

	public void sendMap() {
		List<IChatroomAdapter> removeLater = new ArrayList<IChatroomAdapter>();

		for (IChatroomAdapter room : userChatroomAdapters) {
			new Thread(() -> {
				try {
					room.sendChatroomMessage(new DataPacket<MapMessage>(MapMessage.class,
							new MapMessage()), this.thisAdapter);
				} catch (RemoteException e) {
					removeLater.add(room);
				}
			}).start();
		}
	}

	/**
	 * @return the name of this chatroom
	 */
	@Override
	public String toString() {
		return this.chatroomId.toString();
	}
}
