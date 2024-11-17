import java.sql.*;
import java.util.Scanner;

public class HostelReservationSystem {
    
    // Database connection method
    private static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/hostel_reservation";
        String user = "root";  // Replace with your MySQL username
        String password = "12345";  // Replace with your MySQL password
        return DriverManager.getConnection(url, user, password);
    }

    public static void sendNotification(int userId, String message) {
        String query = "INSERT INTO Notifications (user_id, message) VALUES (?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, message);
            pstmt.executeUpdate();
            System.out.println("Notification sent successfully to user ID: " + userId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method for users to view their notifications
    public static void viewNotifications(int userId) {
        String query = "SELECT message, timestamp FROM Notifications WHERE user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            System.out.println("Your Notifications:");
            while (rs.next()) {
                System.out.println("- [" + rs.getTimestamp("timestamp") + "] " + rs.getString("message"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to check available rooms
    public static void checkRoomAvailability() {
        String query = "SELECT room_id, room_number, room_type, availability_status FROM Rooms WHERE availability_status = 'available'";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                System.out.println("Room ID: " + rs.getInt("room_id") + 
                                   ", Room Number: " + rs.getString("room_number") + 
                                   ", Type: " + rs.getString("room_type") + 
                                   ", Status: " + rs.getString("availability_status"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to book a room
    public static void bookRoom(int userId, int roomId, String checkInDate, String checkOutDate) {
        String query = "INSERT INTO Bookings (user_id, room_id, check_in_date, check_out_date, status) VALUES (?, ?, ?, ?, 'pending')";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, roomId);
            pstmt.setDate(3, Date.valueOf(checkInDate));
            pstmt.setDate(4, Date.valueOf(checkOutDate));
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                updateRoomAvailability(roomId, "booked");
                System.out.println("Room booked successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to cancel booking
    public static void cancelBooking(int bookingId, int roomId) {
        String query = "UPDATE Bookings SET status = 'canceled' WHERE booking_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, bookingId);
            pstmt.executeUpdate();
            updateRoomAvailability(roomId, "available");
            System.out.println("Booking canceled successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to update room availability
    private static void updateRoomAvailability(int roomId, String status) {
        String query = "UPDATE Rooms SET availability_status = ? WHERE room_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, status);
            pstmt.setInt(2, roomId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to make payment for a booking
    public static void processPayment(int bookingId, double amount) {
        String query = "INSERT INTO Payments (booking_id, amount, payment_status) VALUES (?, ?, 'completed')";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, bookingId);
            pstmt.setDouble(2, amount);
            pstmt.executeUpdate();
            System.out.println("Payment processed successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Admin: Add new room
    public static void addRoom(String roomNumber, String roomType, double price) {
        String query = "INSERT INTO Rooms (room_number, room_type, price, availability_status) VALUES (?, ?, ?, 'available')";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, roomNumber);
            pstmt.setString(2, roomType);
            pstmt.setDouble(3, price);
            pstmt.executeUpdate();
            System.out.println("Room added successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Admin: View all rooms
    public static void viewAllRooms() {
        String query = "SELECT * FROM Rooms";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                System.out.println("Room ID: " + rs.getInt("room_id") + 
                                   ", Room Number: " + rs.getString("room_number") + 
                                   ", Type: " + rs.getString("room_type") + 
                                   ", Price: " + rs.getDouble("price") + 
                                   ", Status: " + rs.getString("availability_status"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static String loginUser(String username, String password) {
        String query = "SELECT user_type FROM Users WHERE username = ? AND password = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("user_type");
            } else {
                System.out.println("Invalid username or password.");
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // User: Register new user
    public static void registerUser(String username, String password, String email) {
        String query = "INSERT INTO Users (username, password, email, user_type) VALUES (?, ?, ?, 'guest')";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, email);
            pstmt.executeUpdate();
            System.out.println("User registered successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Main method to run the application
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.println("1. Register User");
            System.out.println("2. Login");
            System.out.println("3. Check Room Availability");
            System.out.println("4. Book Room");
            System.out.println("5. Cancel Booking");
            System.out.println("6. Admin: Add Room");
            System.out.println("7. Admin: View All Rooms");
            System.out.println("8. Process Payment");
            System.out.println("9. Process Payment");
            System.out.println("10. Process Payment");
            System.out.println("11. Exit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();  // Consume newline
            
            switch (choice) {
                case 1: // Register user
                    System.out.print("Enter username: ");
                    String username = scanner.nextLine();
                    System.out.print("Enter password: ");
                    String password = scanner.nextLine();
                    System.out.print("Enter email: ");
                    String email = scanner.nextLine();
                    registerUser(username, password, email);
                    break;
                case 2: // Login
                System.out.print("Enter username: ");
                String loginUsername = scanner.nextLine();
                System.out.print("Enter password: ");
                String loginPassword = scanner.nextLine();
                String userType = loginUser(loginUsername, loginPassword);
                
                if (userType != null) {
                    System.out.println("Login successful! User Type: " + userType);
                    
                    if ("admin".equalsIgnoreCase(userType)) {
                        // Admin dashboard functionality
                    } else {
                        // Guest dashboard functionality
                    }
                }
                break;
                case 3: // Check room availability
                    checkRoomAvailability();
                    break;
                case 4: // Book room
                    System.out.print("Enter user ID: ");
                    int userId = scanner.nextInt();
                    System.out.print("Enter room ID: ");
                    int roomId = scanner.nextInt();
                    System.out.print("Enter check-in date (YYYY-MM-DD): ");
                    String checkInDate = scanner.next();
                    System.out.print("Enter check-out date (YYYY-MM-DD): ");
                    String checkOutDate = scanner.next();
                    bookRoom(userId, roomId, checkInDate, checkOutDate);
                    break;
                case 5: // Cancel booking
                    System.out.print("Enter booking ID: ");
                    int bookingId = scanner.nextInt();
                    System.out.print("Enter room ID: ");
                    int cancelRoomId = scanner.nextInt();
                    cancelBooking(bookingId, cancelRoomId);
                    break;
                case 6: // Admin: Add room
                    System.out.print("Enter room number: ");
                    String roomNumber = scanner.nextLine();
                    System.out.print("Enter room type: ");
                    String roomType = scanner.nextLine();
                    System.out.print("Enter room price: ");
                    double price = scanner.nextDouble();
                    addRoom(roomNumber, roomType, price);
                    break;
                case 7: // Admin: View all rooms
                    viewAllRooms();
                    break;
                case 8: // Process payment
                    System.out.print("Enter booking ID: ");
                    int paymentBookingId = scanner.nextInt();
                    System.out.print("Enter payment amount: ");
                    double amount = scanner.nextDouble();
                    processPayment(paymentBookingId, amount);
                    break;
                case 9:
                    System.out.print("Enter user ID: ");
                    int notifyUserId = scanner.nextInt();
                    scanner.nextLine();
                    System.out.print("Enter message: ");
                    String message = scanner.nextLine();
                    sendNotification(notifyUserId, message);
                    break;
                case 10:
                    viewNotifications(userId);
                    break;
                case 11: // Exit
                    System.out.println("Goodbye!");
                    scanner.close();
                    return;
                default:
                System.out.println("Invalid option. Please try again.");
                break;
        }
    }
}
}

                    
