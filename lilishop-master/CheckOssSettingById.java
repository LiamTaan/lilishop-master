import java.sql.*;
public class CheckOssSettingById {
  public static void main(String[] args) throws Exception {
    Class.forName("com.mysql.cj.jdbc.Driver");
    try (Connection conn = DriverManager.getConnection(
        "jdbc:mysql://127.0.0.1:3306/lilishop?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai",
        "root",
        "TanLiMing123!");
      PreparedStatement ps = conn.prepareStatement("select id, setting_value from li_setting where id = ?");) {
      ps.setString(1, "OSS_SETTING");
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          System.out.println("ID=" + rs.getString(1));
          System.out.println(rs.getString(2));
        } else {
          System.out.println("NO_ROW");
        }
      }
    }
  }
}
