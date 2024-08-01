package ch05;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public abstract class AbstractServer {

	private ServerSocket serverSocket;
	private Socket socket;
	private BufferedReader readerStream;
	private PrintWriter writerStream;
	private BufferedReader keyboardReader;
	
	// set 메서드
	// 메서드 의존 주입 (멤버 변수에 참조 변수를 할당)
	protected void setServerSocket(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}
	
	// 메서드 의존 주입 (멤버 변수에 참조 변수를 할당)
	protected void setSocket(Socket socket) {
		this.socket = socket;
	}
	
	// get 메서드
	protected ServerSocket getServerSocket() {
		return this.serverSocket;
	}
	
	// 실행의 흐름이 필요 (순서 중요)
	public final void run() {
		// 1. 서버 셋팅 - 포트 번호 할당
		try {
			setupServer();
			connection();
			setupStream();
			startService(); // 내부적으로 while 동작
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			cleanup();
		}
	}
	
	// 1. 포트 번호 할당 (구현 클래스에서 직접 설계)
	protected abstract void setupServer() throws IOException;
	
	// 2. 클라이언트 연결 대기 실행 (구현 클래스)
	protected abstract void connection() throws IOException;
	
	// 3. 스트림 초기화 (연결된 소켓에서 스트림을 뽑아내야 함) - 이곳에서 한다
	private void setupStream() throws IOException {
		readerStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		writerStream = new PrintWriter(socket.getOutputStream(),true);
		keyboardReader = new BufferedReader(new InputStreamReader(System.in));
	}
	// 4. 서비스 시작
	private void startService() {
		Thread readThread = createReadThread();
		Thread writeThread = createWriteThread();
		
		readThread.start();
		writeThread.start();
	}
	
	// 캡슐화
	private Thread createReadThread() {
		return new Thread(() -> {
			
			try {
				String msg;
				while( (msg = readerStream.readLine()) != null ) {
					// 서버 측 콘솔에 출력
					System.out.println("client 측 메시지 : " + msg);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		});
	}
	
	private Thread createWriteThread() {
		return new Thread(() -> {
			try {
				String msg;
				// 서버 측 키보드에서 데이터를 한 줄 라인으로 읽음
				while( (msg = keyboardReader.readLine()) != null ) {
					// 클라이언트와 연결된 소켓에 데이터를 보냄
					writerStream.println(msg);
					writerStream.flush();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	// 캡슐화 - 소켓 자원 종료
	private void cleanup() {
		try {
			if(socket != null) {
				socket.close();
			}
			
			if(serverSocket != null) {
				serverSocket.close();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	
	
	
}
