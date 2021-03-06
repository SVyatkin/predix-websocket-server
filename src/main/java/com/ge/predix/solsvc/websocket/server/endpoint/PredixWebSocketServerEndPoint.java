package com.ge.predix.solsvc.websocket.server.endpoint;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@ServerEndpoint(value = "/livestream/{nodeId}")
public class PredixWebSocketServerEndPoint {
	private static Logger logger = LoggerFactory.getLogger(PredixWebSocketServerEndPoint.class);
	
	/**
	 * @param nodeId1
	 *            - nodeId for the session
	 * @param session
	 *            - session object
	 * @param ec
	 *            -
	 */
	@OnOpen
	public void onOpen(@PathParam(value = "nodeId") String nodeId, final Session session, EndpointConfig ec) {
		logger.info("Server: opened... for Node Id : " + nodeId + " : " + session.getId()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@OnMessage
	public void onMessage(@PathParam(value = "nodeId") String nodeId, String message, Session session) {
		logger.info("Websocket Message : " + message);
		try {
			if ("messages".equalsIgnoreCase(nodeId)) { //$NON-NLS-1$
				JsonParser parser = new JsonParser();
				JsonObject o = (JsonObject) parser.parse(message);
				JsonArray nodes = o.getAsJsonArray("body"); //$NON-NLS-1$
				for (Session s : session.getOpenSessions()) {
					if (!"messages".equals(s.getPathParameters().get("nodeId"))) { //$NON-NLS-1$ //$NON-NLS-2$
						String pNodeName = s.getPathParameters().get("nodeId"); //$NON-NLS-1$
						JsonObject node = findJsonObjectByName(nodes, pNodeName);
						if (node != null) {
							s.getBasicRemote().sendText(node.toString());
						}
					}
				}
				String response = "{\"messageId\": " + o.get("messageId") + ",\"statusCode\": 202}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				session.getBasicRemote().sendText(response);
			} else {
				session.getBasicRemote().sendText("SUCCESS"); //$NON-NLS-1$
			}
		} catch (Exception ex) {
			logger.error("Exception in onMessage ", ex); //$NON-NLS-1$
			throw new RuntimeException(ex);
		}
	}

	private JsonObject findJsonObjectByName(JsonArray nodes, String pNodeName) {
		for (int i = 0; i < nodes.size(); i++) {
			JsonObject node = (JsonObject) nodes.get(i);
			String nodeName = node.get("name").getAsString(); //$NON-NLS-1$
			if (pNodeName.equalsIgnoreCase(nodeName.trim())) {
				return node;
			}
		}
		return null;
	}

	/**
	 * @param session
	 *            - session object
	 * @param closeReason
	 *            - The reason of close of session
	 */
	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		logger.info("Server: Session " + session.getId() + " closed because of " + closeReason.toString()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @param session
	 *            - current session object
	 * @param t
	 *            - Throwable instance containing error info
	 */
	@OnError
	public void onError(Session session, Throwable t) {
		logger.error("Server: Session " + session.getId() + " closed because of " + t.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
