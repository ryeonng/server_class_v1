package ch06;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class MultiClientServer {

	private static final int PORT = 5000; // 포트 번호 지정
	// 하나의 변수에 자원을 통으로 관리 하기위한 기법 : 자료구조
	// 단일 스레드 / 멀티 스레드 -> 멀티 스레드 일 때 어떤 자료구조를 선택해야 하는 가 ?
	// 객체 배열 <- Vector<> : 멀티 스레드에 안정적이다.
	private static Vector<PrintWriter> clientWriters = new Vector<>();
	
	public static void main(String[] args) {
		System.out.println("Server started ...");
		
		try (ServerSocket serverSocket = new ServerSocket(PORT)){
				
			while(true) {
				// 1. serverSocket.accept() 호출 하면, blocking 상태가 된다.
				// 2. 클라이언트가 연결을 요청하면, 새로운 소켓 객체가 생성 된다.
				// 3. 새로운 스레드를 만들어 처리. (클라이언트가 데이터를 주고 받기 위한 스레드)
				// 4. 새로운 클라이언트가 접속 하기 까지 다시 대기 유지. (계속 반복 시킴)
				Socket socket = serverSocket.accept();
				
				// 새로운 클라이언트가 연결 되면, 새로운 스레드 생성 됨
				new ClientHandler(socket).start(); 
				
			}
			
		} catch (Exception e) {

		}
		
	} // end of main
	
	// 정적 내부 클래스 설계
	private static class ClientHandler extends Thread{
		
		private Socket socket; // 클라이언트와 연결 된 소켓
		private PrintWriter out;
		private BufferedReader in;
		
		public ClientHandler(Socket socket) { // 소켓을 주입 받을 수 있게 처리
			this.socket = socket;
		}
		
		// 스레드 start() 호출 시, 동작 되는 메서드 - 약속
		@Override
		public void run() {
			
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(),true);
				
				// ★★★★★ - 서버가 관리하는 자료구조에 자원을 저장 (클라이언트와 연결된 소켓 안 - outstream)
				clientWriters.add(out);
				
				String message;
				while( (message = in.readLine() ) != null ) {
					System.out.println("Received : " + message);
					broadcastMessage(message);
				}
				
				
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					socket.close();
					System.out.println("▶▶▶ 클라이언트 연결 해제 ◀◀◀");
				} catch (IOException e) {
					// e.printStackTrace();
				}
			}
			
		}
		
	}// end of ClientHandler
	
	// 모든 클라이언트에게 메세지 보내기 - broadcast
	private static void broadcastMessage(String message) {
		
		for(PrintWriter writer : clientWriters) {
			writer.println(message);
		}
		
	}
	 
} // end of class
