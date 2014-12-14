package yn4_jjc7.client.model;

import java.net.Inet4Address;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import provided.datapacket.ADataPacketAlgoCmd;
import provided.datapacket.DataPacket;
import provided.datapacket.DataPacketAlgo;
import provided.rmiUtils.IRMIUtils;
import provided.rmiUtils.IRMI_Defs;
import provided.rmiUtils.RMIUtils;
import provided.util.IVoidLambda;
import yn4_jjc7.client.model.chatroom.Chatroom;
import yn4_jjc7.client.model.chatroom.IChatroomToModelAdapter;
import yn4_jjc7.client.model.chatroom.IChatroomToViewAdapter;
import yn4_jjc7.client.model.message.connect.*;
import common.ICmd2ModelAdapter;
import common.chatroom.IChatroomID;
import common.message.INullMessage;
import common.message.NullMessage;
import common.message.connect.*;
import common.user.IUser;
import common.user.IUserRMIWrapper;

/**
 * The main model for the ChatApp application.
 */
public class ChatAppModel {

	/** Adapter to the main view */
	private IModelToViewAdapter view;

	/** The RMI registry */
	private Registry registry;

	/** The list of chatrooms that this user is connected to */
	private ArrayList<Chatroom> chatrooms;

	/** The user adapter for the local user */
	private IUser thisUser;

	/** The user stub for the local user */
	private IUserRemote thisUserStub;

	/** The RMI wrapper stub */
	private IUserRMIWrapper registryWrapperRemote;

	/** The RMI wrapper that will be serialized and sent to a user */
	private IUserRMIWrapper registryWrapper;

	/** The remote implementation that will be used by another user via the stub sent over the network. */
	private IUserRemote thisUserRemote = new IUserRemote() {

		@Override
		public DataPacket<? extends IConnectMessage> sendMessage(
				DataPacket<? extends IConnectMessage> message, IUser sender) throws RemoteException {
			return message.execute(connectVisitor, sender);
		}

	};
	
	/** TODO comment */
	private DataPacketAlgo<Void, IUser> connectReturnVisitor = new DataPacketAlgo<Void, IUser>(

			/* ==================== Default case ==================== */
			new ADataPacketAlgoCmd<Void, Object, IUser>() {
				
				private static final long serialVersionUID = -658674921830186479L;

				@Override
				public Void apply(Class<?> index, DataPacket<Object> host, IUser... params) {
					host.execute(connectVisitor, params);
					return null;
				}

				@Override
				public void setCmd2ModelAdpt(ICmd2ModelAdapter cmd2ModelAdpt) {
				}

			}) {
		
		private static final long serialVersionUID = -658674921830186479L;

		/* Add commands to visitor */
		{
			
			/* ==================== Null Message case ==================== */
			setCmd(INullMessage.class,
					new ADataPacketAlgoCmd<Void, Object, IUser>() {

						/** Generated serial version UID */
						private static final long serialVersionUID = -5977122303584840531L;

						@Override
						public Void apply(Class<?> index, DataPacket<Object> host, IUser... params) {
							return null;
						}

						@Override
						public void setCmd2ModelAdpt(ICmd2ModelAdapter cmd2ModelAdpt) {
						}

					});

		}
	};

	/** The visitor that will be used to process an <code>IConnectMessage</code> */
	private DataPacketAlgo<DataPacket<? extends IConnectMessage>, IUser> connectVisitor = new DataPacketAlgo<DataPacket<? extends IConnectMessage>, IUser>(

			/* ==================== Default case ==================== */
			new ADataPacketAlgoCmd<DataPacket<? extends IConnectMessage>, Object, IUser>() {

				/** Generated serial version UID */
				private static final long serialVersionUID = 976090757836872293L;

				@Override
				public DataPacket<? extends IConnectMessage> apply(Class<?> index,
						DataPacket<Object> host, IUser... params) {
					return new DataPacket<INullMessage>(INullMessage.class, NullMessage.SINGLETON);
				}

				@Override
				public void setCmd2ModelAdpt(ICmd2ModelAdapter cmd2ModelAdpt) {
				}

			}) {

		/** Generated serial version UID */
		private static final long serialVersionUID = -5037811677497093069L;

		/* Add commands to visitor */
		{

			/* ==================== Null Message case ==================== */
			setCmd(INullMessage.class,
					new ADataPacketAlgoCmd<DataPacket<? extends IConnectMessage>, Object, IUser>() {

						/** Generated serial version UID */
						private static final long serialVersionUID = -5977122303584840531L;

						@Override
						public DataPacket<? extends IConnectMessage> apply(Class<?> index,
								DataPacket<Object> host, IUser... params) {
							return new DataPacket<INullMessage>(INullMessage.class, NullMessage.SINGLETON);
						}

						@Override
						public void setCmd2ModelAdpt(ICmd2ModelAdapter cmd2ModelAdpt) {
						}

					});

			/* ==================== Get Chatrooms case ==================== */
			setCmd(IGetChatroomsListMessage.class,
					new ADataPacketAlgoCmd<DataPacket<? extends IConnectMessage>, Object, IUser>() {

						/** Generated serial version UID */
						private static final long serialVersionUID = 8854074182290960305L;

						/**
						 * Get chatrooms case: called when a user requests a list of chatrooms that this user is
						 * connected to.
						 */
						@Override
						public DataPacket<? extends IConnectMessage> apply(Class<?> index,
								DataPacket<Object> host, IUser... params) {

							ArrayList<IChatroomID> chatroomIds = new ArrayList<IChatroomID>();
							for (Chatroom c : chatrooms) {
								chatroomIds.add(c.getChatroomID());
							}
							ChatroomsListMessage messageToSend = new ChatroomsListMessage(chatroomIds);

							return new DataPacket<IChatroomsListMessage>(IChatroomsListMessage.class, messageToSend);
						}

						@Override
						public void setCmd2ModelAdpt(ICmd2ModelAdapter cmd2ModelAdpt) {
						}

					});

			/* ==================== List Chatrooms case ==================== */
			setCmd(IChatroomsListMessage.class,
					new ADataPacketAlgoCmd<DataPacket<? extends IConnectMessage>, Object, IUser>() {

						/** Generated serial version UID */
						private static final long serialVersionUID = -5309325789459930104L;

						/**
						 * The case: called in response to receiving a list of chatrooms from a remote user
						 */
						@Override
						public DataPacket<? extends IConnectMessage> apply(Class<?> index,
								DataPacket<Object> host, IUser... params) {

							IChatroomsListMessage chatlistMessage = (IChatroomsListMessage) host.getData();
							IUser remoteUser = (IUser) params[0];
							List<IChatroomID> roomIds = chatlistMessage.getChatroomIDs();
							List<IChatroomID> nonDuplicateRoomIds = new ArrayList<IChatroomID>(roomIds);

							for (Chatroom aChatroom : chatrooms) {
								nonDuplicateRoomIds.remove(aChatroom.getChatroomID());
							}

							IChatroomID roomToJoin = view.chooseChatroomToJoin(nonDuplicateRoomIds); // roomIds
							if (roomToJoin != null) {
								new Thread(() -> {
									try {
										DataPacket<IRequestChatroomMessage> requestToJoin =
												new DataPacket<>(IRequestChatroomMessage.class,
														new RequestChatroomMessage(roomToJoin, thisUser.toString()));
										DataPacket<? extends IConnectMessage> response = remoteUser.sendMessage(
												requestToJoin, thisUser);
										response.execute(connectVisitor, remoteUser);
									} catch (RemoteException e) {
										e.printStackTrace();
									}
								}).start();
							}

							return new DataPacket<INullMessage>(INullMessage.class, NullMessage.SINGLETON);
						}

						@Override
						public void setCmd2ModelAdpt(ICmd2ModelAdapter cmd2ModelAdpt) {
						}

					});

			/* ==================== Connect back case ==================== */
			setCmd(IConnectBack.class,
					new ADataPacketAlgoCmd<DataPacket<? extends IConnectMessage>, Object, IUser>() {

						/** Generated serial version UID */
						private static final long serialVersionUID = 8690754477587685372L;

						@Override
						public DataPacket<? extends IConnectMessage> apply(Class<?> index,
								DataPacket<Object> host, IUser... params) {
							return new DataPacket<INullMessage>(INullMessage.class, NullMessage.SINGLETON);
						}

						@Override
						public void setCmd2ModelAdpt(ICmd2ModelAdapter cmd2ModelAdpt) {
						}

					});

			/* ==================== Reject Request case ==================== */
			setCmd(IRejectRequestMessage.class,
					new ADataPacketAlgoCmd<DataPacket<? extends IConnectMessage>, Object, IUser>() {

						/** Generated serial version UID */
						private static final long serialVersionUID = -4701527526515518667L;

						/**
						 * Reject request case: called in response to a rejection to join a chatroom
						 */
						@Override
						public DataPacket<? extends IConnectMessage> apply(Class<?> index,
								DataPacket<Object> host, IUser... params) {
							view.showInformationDialog("Request rejected", "Your request to join was rejected.");
							return new DataPacket<INullMessage>(INullMessage.class, NullMessage.SINGLETON);
						}

						@Override
						public void setCmd2ModelAdpt(ICmd2ModelAdapter cmd2ModelAdpt) {
						}

					});

			/* ==================== Request chatroom case ==================== */
			setCmd(IRequestChatroomMessage.class,
					new ADataPacketAlgoCmd<DataPacket<? extends IConnectMessage>, Object, IUser>() {

						/** Generated serial version UID */
						private static final long serialVersionUID = -7733576456349090114L;

						/**
						 * Request chatroom case: called in response to a remote request to join one of our chatrooms
						 */
						@Override
						public DataPacket<? extends IConnectMessage> apply(Class<?> index,
								DataPacket<Object> host, IUser... params) {
							IRequestChatroomMessage request = (IRequestChatroomMessage) host.getData();

							for (Chatroom room : chatrooms) {
								if (room.getChatroomID().equals(request.getChatroomID())) {
									boolean shouldInvite = view.displayQuestionMessage(
											"Join Request",
											String.format("%s has requested to join %s. Accept?",
													request.getName(), request.getChatroomID().toString()));

									if (shouldInvite) {
										ChatroomInviteMessage invite = new ChatroomInviteMessage(
												room.getChatroomID(),
												room.getUserStubs(), thisUser.toString());
										return new DataPacket<IChatroomInviteMessage>(
												IChatroomInviteMessage.class, invite);
									} else {
										return new DataPacket<IRejectRequestMessage>(
												IRejectRequestMessage.class,
												new RejectRequestMessage(request.getChatroomID()));
									}
								}
							}
							return new DataPacket<INullMessage>(INullMessage.class, NullMessage.SINGLETON);
						}

						@Override
						public void setCmd2ModelAdpt(ICmd2ModelAdapter cmd2ModelAdpt) {
						}

					});

			/* ==================== Invite chatroom case ==================== */
			setCmd(IChatroomInviteMessage.class,
					new ADataPacketAlgoCmd<DataPacket<? extends IConnectMessage>, Object, IUser>() {

						/** Generated serial version UID */
						private static final long serialVersionUID = 3365045343276416086L;

						/**
						 * Invite chatroom case: called in response to a user inviting us to one of their chatrooms
						 */
						@Override
						public DataPacket<? extends IConnectMessage> apply(Class<?> index,
								DataPacket<Object> host, IUser... params) {
							IChatroomInviteMessage invite = (IChatroomInviteMessage) host.getData();

							System.out.println("I was invited to " + invite.getChatroomID());

							boolean shouldJoin = view.displayQuestionMessage("Chatroom Invite",
									String.format("%s has invited you to join %s. Accept?",
											invite.getName(), invite.getChatroomID()));

							if (shouldJoin) {
								Chatroom newRoom = new Chatroom(invite.getChatroomID(), thisUser,
										IChatroomToViewAdapter.NULL_OBJECT,
										new IChatroomToModelAdapter() {

											@Override
											public void removeChatroom(Chatroom room) {
												chatrooms.remove(room);
											}

										}, invite.getMemberAdapters());
								newRoom.installViewAdapter(view.makeChatroomToViewAdapter(newRoom));
								chatrooms.add(newRoom);
							}

							return new DataPacket<INullMessage>(INullMessage.class, NullMessage.SINGLETON);
						}

						@Override
						public void setCmd2ModelAdpt(ICmd2ModelAdapter cmd2ModelAdpt) {
						}
						
					});
		}
	};

	/** Factory for the Registry and other uses. */
	IRMIUtils rmiUtils = new RMIUtils(new IVoidLambda<String>() {
		@Override
		public void apply(String... params) {
			// no-op
		}
	});

	/**
	 * Creates a new ChatApp model with the specified view adapter.
	 * 
	 * @param view the adapter to connect this model with its view
	 */
	public ChatAppModel(IModelToViewAdapter view) {
		this.view = view;
		this.chatrooms = new ArrayList<Chatroom>();
	}

	/**
	 * Makes a brand new chatroom with the specified name.
	 * 
	 * @param name the name of the new chatroom to create
	 */
	public void makeRoom(String name) {
		Chatroom room;

		for (Chatroom theRoom : chatrooms) {
			if (theRoom.getName().equals(name)) {
				view.showErrorDialog("Error", "This chatroom name is already in use.");
				return;
			}
		}

		room = new Chatroom(name, thisUser, IChatroomToViewAdapter.NULL_OBJECT, new IChatroomToModelAdapter() {

			@Override
			public void removeChatroom(Chatroom room) {
				chatrooms.remove(room);
			}

		});

		room.installViewAdapter(view.makeChatroomToViewAdapter(room));
		chatrooms.add(room);
	}

	/**
	 * Starts the model. Specifically, initializes the RMI server and exports our remote user object so that other users
	 * can connect to us.
	 */
	public void start() {
		rmiUtils.startRMI(IRMI_Defs.CLASS_SERVER_PORT_SERVER);

		try {
			this.thisUserStub = (IUserRemote) UnicastRemoteObject.exportObject(thisUserRemote, 2101);

			this.thisUser = new User(System.getProperty("user.name"),
					(Inet4Address) Inet4Address.getLocalHost(), thisUserStub);

			this.registry = rmiUtils.getLocalRegistry();

			this.registryWrapper = new IUserRMIWrapper() {

				@Override
				public IUser getUser() throws RemoteException {
					return thisUser;
				}
			};

			this.registryWrapperRemote = (IUserRMIWrapper) UnicastRemoteObject.exportObject(registryWrapper, 2101);

			this.registry.rebind(IUserRMIWrapper.BOUND_NAME_CLIENT, registryWrapperRemote);
		} catch (Exception e) {
			System.err.println("ComputeEngine exception:" + "\n");
			e.printStackTrace();
			System.exit(-1);
		}

	}

	/**
	 * Stops the model. Specifically, shuts down the RMI server.
	 */
	public void stop() {
		try {
			rmiUtils.stopRMI();
		} catch (Exception e) {
			System.err.println("Error stopping RMI server: " + e);
		}
		System.exit(0);
	}

	/**
	 * Returns a list of the chatrooms that this user is currently connected to.
	 * 
	 * @return a list of the chatrooms that this user is currently connected to
	 */
	public List<Chatroom> getConnectedChatrooms() {
		return this.chatrooms;
	}

	/**
	 * Leaves all chatrooms that are currently connected.
	 */
	public void leaveAllChatrooms() {
		// leaveRoom removes the chatroom from chatrooms, creating new list prevents concurrent modification exception
		for (Chatroom room : new ArrayList<Chatroom>(chatrooms)) {
			room.leaveRoom();
		}
	}

	/**
	 * Asynchronously sends a remote invitation to the specified IP address inviting that user to a chatroom. Will call
	 * the GUI to request the specific chatroom to invite.
	 * 
	 * @param ipAddress the IP address of the user to invite
	 */
	public void inviteToChatroom(String ipAddress) {
		try {
			Registry registry = rmiUtils.getRemoteRegistry(ipAddress);
			IUser remoteUser = ((IUserRMIWrapper) registry.lookup(IUserRMIWrapper.BOUND_NAME_CLIENT)).getUser();

			Chatroom inviteTo = view.chooseChatroomToInvite(this.chatrooms);

			if (inviteTo != null) {
				new Thread(() -> {
					DataPacket<? extends IConnectMessage> response;
					try {
						response = remoteUser.sendMessage(
								new DataPacket<IChatroomInviteMessage>(IChatroomInviteMessage.class,
										new ChatroomInviteMessage(inviteTo.getChatroomID(),
												inviteTo.getUserStubs(), thisUser.toString())), thisUser);
						response.execute(connectReturnVisitor, remoteUser);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}).start();

			}
		} catch (NotBoundException e) {
			e.printStackTrace();
		} catch (AccessException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Asynchronously sends a join request to the user at the specified IP address. Will pop up a dialog on the GUI
	 * asking the user to specify which chatroom to request to join.
	 * 
	 * @param ipAddress the IP address of the user to send the join request to
	 */
	public void requestToJoin(String ipAddress) {
		try {
			Registry registry = rmiUtils.getRemoteRegistry(ipAddress);
			IUser remoteUser = ((IUserRMIWrapper) registry.lookup(IUserRMIWrapper.BOUND_NAME_CLIENT)).getUser();

			new Thread(() -> {
				DataPacket<? extends IConnectMessage> response;
				try {
					response = remoteUser.sendMessage(new DataPacket<IGetChatroomsListMessage>(
							IGetChatroomsListMessage.class, new GetChatroomsListMessage()), thisUser);
					response.execute(connectReturnVisitor, remoteUser);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}).start();

			System.out.println("connected to: " + remoteUser);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}

}