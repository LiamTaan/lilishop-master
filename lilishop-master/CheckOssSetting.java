import java.sql.*;
public class CheckOssSetting {
  public static void main(String[] args) throws Exception {
    String[] urls = {
      "jdbc:mysql://127.0.0.1:3306/lilishop?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai"
    };
    String[] passwords = {"TanLiMing123!", "123456", "lilishop"};
    Class.forName("com.mysql.cj.jdbc.Driver");
    for (String url : urls) {
      for (String password : passwords) {
        try (Connection conn = DriverManager.getConnection(url, "root", password);
             PreparedStatement ps = conn.prepareStatement("select setting_value from li_setting where setting_key = ?");) {
          ps.setString(1, "OSS_SETTING");
          try (ResultSet rs = ps.executeQuery()) {
            System.out.println("CONNECTED password=" + password);
            if (rs.next()) {
              System.out.println(rs.getString(1));
            } else {
              System.out.println("NO_ROW");
            }
            return;
          }
        } catch (Exception e) {
          System.out.println("FAILED password=" + password + " reason=" + e.getClass().getSimpleName() + ":" + e.getMessage());
        }
      }
    }
  }
}
