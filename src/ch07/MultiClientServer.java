package ch07;

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
				Socket socket = serverSocket.accept();
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
				
				// 코드 추가
				// 클라이언트로부터 이름 받기(약속되어 있음 !)
				String nameMessage = in.readLine();
				if(nameMessage != null && nameMessage.startsWith("NAME:")) {
					String clientName = nameMessage.substring(5);
					broadcastMessage("해당 서버에 : " + clientName + " 님 입장");
				} else {
					// 약속과 다르게 접근 했다면, 종료 처리 할 것이다.
					socket.close();
					return;
				}
				
				
				// ★★★★★ - 서버가 관리하는 자료구조에 자원을 저장 (클라이언트와 연결된 소켓 안 - outstream)
				clientWriters.add(out);
				
				String message;
				while( (message = in.readLine() ) != null ) {
					System.out.println("Received : " + message);
					
					// 클라이언트와 서버의 약속
					// :를 기준으로 처리
					// MSG:안녕\n
					String[] parts = message.split(":", 2);
					System.out.println("parts 인덱스 개수 : " + parts.length);
					// 명령 부분을 분리
					String command = parts[0];
					// 데이터 부분을 분리
					String data = parts.length > 1 ? parts[1] : "";
					System.out.println("command : " + command);
					System.out.println("data : " + data);
					
					if(command.equals("MSG")) {
						System.out.println("연결된 전체 사용자에게 MSG 방송");
						broadcastMessage(message);
					} else if(command.equals("BYE")) {
						
						System.out.println("Client disconnected ...");
						break; // while 구문 종료
					}
					
				} // end of while
				// 이 다음 코드가 finally 구문으로 빠진다.
				
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					socket.close();
					// * 도전 과제
					// 서버 측에서 관리하고 있는 PrintWriter 를 제거 해야 한다.
					// 인덱스 번호가 필요함
					// clientWriters.add() 할 때, 지정된 나의 인덱스 번호가 필요하다.
					//clientWriters.remove();
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
