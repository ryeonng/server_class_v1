package ch05;

import java.io.IOException;
import java.net.ServerSocket;

public class MyThreadServer extends AbstractServer {

	@Override
	protected void setupServer() throws IOException {
		// 추상 클래스가 부모에 있고, 자식은 부모 기능을 확장 / 사용 가능하다.
		// 서버 측 소켓 통신 준비물 : 서버 소켓
		super.setServerSocket(new ServerSocket(5000));
		System.out.println(">>> Server started on Port 5000 <<<");
	}

	@Override
	protected void connection() throws IOException {
		// 서버소켓.accept() 호출
		super.setSocket(super.getServerSocket().accept());
	}
	
	public static void main(String[] args) {
		MyThreadServer myThreadServer = new MyThreadServer();
		myThreadServer.run();
	}

}
