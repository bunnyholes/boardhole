package bunny.boardhole.controller;

import bunny.boardhole.domain.Member;
import bunny.boardhole.service.MemberServiceRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
public class MemberControllerCLI implements CommandLineRunner {

    @Autowired
    private MemberServiceRef memberServiceRef;

    private static String currentUser = null; // 현재 로그인된 사용자
    private Scanner scanner = new Scanner(System.in);

    @Override
    public void run(String... args) throws Exception {
        // CLI 활성화 여부 확인
        boolean cliEnabled = args.length > 0 && "--cli".equals(args[0]);
        
        if (cliEnabled) {
            // 별도 스레드에서 CLI 실행 (웹 서버 블로킹 방지)
            Thread cliThread = new Thread(() -> {
                try {
                    Thread.sleep(2000); // 웹 서버 시작 대기
                    System.out.println("\n========================================");
                    System.out.println("🎯 회원 관리 CLI 시작");
                    System.out.println("========================================");
                    runCLI();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            
            cliThread.setDaemon(true); // 메인 애플리케이션 종료시 함께 종료
            cliThread.start();
        } else {
            System.out.println("\n💡 CLI 모드를 사용하려면: ./gradlew bootRun --args='--cli'");
            System.out.println("🌐 웹 API: http://localhost:8080/ref/");
        }
    }

    private void runCLI() {
        boolean running = true;
        
        while (running) {
            try {
                showMenu();
                System.out.print("선택: ");
                
                String choice = scanner.nextLine().trim();
                
                switch (choice) {
                    case "1":
                        handleJoin();
                        break;
                    case "2":
                        handleLogin();
                        break;
                    case "3":
                        handleLogout();
                        break;
                    case "4":
                        handleViewMembers();
                        break;
                    case "5":
                        handleMyInfo();
                        break;
                    case "6":
                        handleExit();
                        running = false;
                        break;
                    default:
                        System.out.println("❌ 잘못된 선택입니다. 1-6 사이의 숫자를 입력하세요.");
                }
                
                if (running) {
                    System.out.println("\n계속하려면 Enter를 누르세요...");
                    scanner.nextLine();
                }
                
            } catch (Exception e) {
                System.out.println("❌ 오류가 발생했습니다: " + e.getMessage());
            }
        }
    }

    private void showMenu() {
        System.out.println("\n========================================");
        System.out.println("🏠 회원 관리 시스템");
        if (currentUser != null) {
            System.out.println("👤 로그인: " + currentUser);
        } else {
            System.out.println("👤 로그인 상태: 로그아웃");
        }
        System.out.println("========================================");
        System.out.println("1. 👥 회원가입");
        System.out.println("2. 🔑 로그인");
        System.out.println("3. 🚪 로그아웃");
        System.out.println("4. 📋 회원 목록 보기");
        System.out.println("5. 👤 내 정보 보기");
        System.out.println("6. 🔚 종료");
        System.out.println("========================================");
    }

    private void handleJoin() {
        System.out.println("\n📝 회원가입");
        System.out.println("----------------------------------------");
        
        System.out.print("회원명 입력: ");
        String memberName = scanner.nextLine().trim();
        
        System.out.print("비밀번호 입력 (4자 이상): ");
        String password = scanner.nextLine().trim();
        
        try {
            memberServiceRef.join(memberName, password);
            System.out.println("✅ 회원가입이 완료되었습니다!");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    private void handleLogin() {
        if (currentUser != null) {
            System.out.println("❌ 이미 로그인되어 있습니다. 현재 사용자: " + currentUser);
            return;
        }
        
        System.out.println("\n🔑 로그인");
        System.out.println("----------------------------------------");
        
        System.out.print("회원명 입력: ");
        String memberName = scanner.nextLine().trim();
        
        System.out.print("비밀번호 입력: ");
        String password = scanner.nextLine().trim();
        
        if (memberServiceRef.login(memberName, password)) {
            currentUser = memberName;
            System.out.println("✅ 로그인 성공! 환영합니다, " + memberName + "님!");
        } else {
            System.out.println("❌ 아이디 또는 비밀번호가 틀렸습니다.");
        }
    }

    private void handleLogout() {
        if (currentUser == null) {
            System.out.println("❌ 로그인되어 있지 않습니다.");
        } else {
            String loggedOutUser = currentUser;
            currentUser = null;
            System.out.println("✅ " + loggedOutUser + "님이 로그아웃되었습니다.");
        }
    }

    private void handleViewMembers() {
        System.out.println("\n📋 전체 회원 목록");
        System.out.println("----------------------------------------");
        
        try {
            String members = memberServiceRef.getMembers();
            if (members.equals("{}")) {
                System.out.println("등록된 회원이 없습니다.");
            } else {
                System.out.println("회원 목록: " + members);
            }
        } catch (Exception e) {
            System.out.println("❌ 회원 목록을 불러오는데 실패했습니다: " + e.getMessage());
        }
    }

    private void handleMyInfo() {
        if (currentUser == null) {
            System.out.println("❌ 로그인이 필요합니다.");
            return;
        }
        
        System.out.println("\n👤 내 정보");
        System.out.println("----------------------------------------");
        
        try {
            Member member = memberServiceRef.getMemberByName(currentUser);
            if (member != null) {
                System.out.println("회원ID: " + member.getId());
                System.out.println("회원명: " + member.getMemberName());
                System.out.println("가입일: " + member.getCreatedAt());
            } else {
                System.out.println("❌ 회원 정보를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            System.out.println("❌ 정보를 불러오는데 실패했습니다: " + e.getMessage());
        }
    }

    private void handleExit() {
        System.out.println("\n👋 CLI를 종료합니다.");
        System.out.println("💡 웹 서버는 계속 실행 중입니다. (http://localhost:8080)");
        System.out.println("=======================================");
    }
}