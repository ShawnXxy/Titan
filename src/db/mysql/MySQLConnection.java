package db.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import db.DBConnection;
import entity.Item;
import entity.Item.ItemBuilder;
import external.ExternalAPI;
import external.ExternalAPIFactory;

// This is a singleton pattern
public class MySQLConnection implements DBConnection {
    
    private static MySQLConnection instance;

    public static DBConnection getInstance() {
        if (instance == null) {
            instance = new MySQLConnection();
        }
        return instance;
    }

    private Connection conn = null;

    private MySQLConnection() {
        try {
            // Forcing the class representing the MySQL driver to load and initialize¡£ The new Instance() call is a work around for some broken Java implementations
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(MySQLDBUtil.URL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) { // ignored

            }
        }
    }

    @Override
    public void setFavoriteItems(String userId, List<String> itemIds) {
        // TODO Auto-generated method stub
        String query = "INSERT INTO history (user_id, item_id) VALUES (?, ?)";
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            for (String itemId : itemIds) {
                statement.setString(1, userId);
                statement.setString(2, itemId);
                statement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unsetFavoriteItems(String userId, List<String> itemIds) {
        // TODO Auto-generated method stub
        String query = "DELETE FROM history WHERE user_id = ? and item_id = ?";
        try {
          PreparedStatement statement = conn.prepareStatement(query);
          for (String itemId : itemIds) {
            statement.setString(1, userId);
            statement.setString(2, itemId);
            statement.execute();
          }
        } catch (SQLException e) {
          e.printStackTrace();
        }
    }

    @Override
    public Set<String> getCategories(String itemId) {
        // TODO Auto-generated method stub
        // return null;

        Set<String> categories = new HashSet<>();
        try {
            String sql = "SELECT category FROM categories WHERE item_id = ? ";
              PreparedStatement statement = conn.prepareStatement(sql);
              statement.setString(1, itemId);
              ResultSet rs = statement.executeQuery();
              while (rs.next()) {
                categories.add(rs.getString("category"));
              }
            } catch (Exception e) {
              System.out.println(e.getMessage());
            }
            return categories;
    }

    @Override
    public List<Item> searchItems(String userId, double lat, double lon, String term) {
        // TODO Auto-generated method stub
        // return null;

     // Connect to external API
        ExternalAPI api = ExternalAPIFactory.getExternalAPI(); // moved here
        List<Item> items = api.search(lat, lon, term);
        for (Item item : items) {
            // Save the item into our own db.
            saveItem(item);
        }
        return items;
    }

    @Override
    public void saveItem(Item item) {
        // TODO Auto-generated method stub
        try {
            // First, insert into items table
            String sql = "INSERT IGNORE INTO items VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, item.getItemId());
            statement.setString(2, item.getName());
//            statement.setString(3, item.getDate());
//            statement.setString(4, item.getTime());
//            statement.setString(5, item.getRemainingTickets());
//            statement.setString(6, item.getSaleStatus());
            statement.setString(3, item.getCity());
            statement.setString(4, item.getState());
            statement.setString(5, item.getCountry());
            statement.setString(6, item.getZipcode());
            statement.setDouble(7, item.getRating());
            statement.setString(8, item.getAddress());
            statement.setDouble(9, item.getLatitude());
            statement.setDouble(10, item.getLongitude());
            statement.setString(11, item.getDescription());
            statement.setString(12, item.getSnippet());
            statement.setString(13, item.getSnippetUrl());
            statement.setString(14, item.getImageUrl());
            statement.setString(15, item.getUrl());
            statement.execute();

            // Second, update categories table for each category.
            sql = "INSERT IGNORE INTO categories VALUES (?,?)";
            for (String category : item.getCategories()) {
                statement = conn.prepareStatement(sql);
                statement.setString(1, item.getItemId());
                statement.setString(2, category);
                statement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }   
    }

    @Override
    public Set<String> getFavoriteItemIds(String userId) {
        // TODO Auto-generated method stub
        // return null;

        Set<String> favoriteItems = new HashSet<>();
        try {
            String sql = "SELECT item_id FROM history WHERE user_id = ?";
              PreparedStatement statement = conn.prepareStatement(sql);
              statement.setString(1, userId);
              ResultSet rs = statement.executeQuery();
              while (rs.next()) {
                String itemId = rs.getString("item_id");
                favoriteItems.add(itemId);
              }
            } catch (SQLException e) {
              e.printStackTrace();
            }
            return favoriteItems;
    }

    @Override
    public Set<Item> getFavoriteItems(String userId) {
        // TODO Auto-generated method stub
//      return null;
        
        Set<String> itemIds = getFavoriteItemIds(userId);
        Set<Item> favoriteItems = new HashSet<>();
        try {
            for (String itemId : itemIds) {
                String sql = "SELECT * from items WHERE item_id = ? ";
                PreparedStatement statement = conn.prepareStatement(sql);
                statement.setString(1, itemId);
                ResultSet rs = statement.executeQuery();
                ItemBuilder builder = new ItemBuilder();

                // Because itemId is unique and given one item id there should have only one result returned.
                if (rs.next()) {
                  builder.setItemId(rs.getString("item_id"));
                  builder.setName(rs.getString("name"));
//                  builder.setDate(rs.getString("date"));
//                  builder.setTime(rs.getString("time"));
//                  builder.setRemainingTickets(rs.getString("remaining_tickets"));
//                  builder.setSaleStatus(rs.getString("sale_status"));
                  builder.setCity(rs.getString("city"));
                  builder.setState(rs.getString("state"));
                  builder.setCountry(rs.getString("country"));
                  builder.setZipcode(rs.getString("zipcode"));
                  builder.setRating(rs.getDouble("rating"));
                  builder.setAddress(rs.getString("address"));
                  builder.setLatitude(rs.getDouble("latitude"));
                  builder.setLongitude(rs.getDouble("longitude"));
                  builder.setDescription(rs.getString("description"));
                  builder.setSnippet(rs.getString("snippet"));
                  builder.setSnippetUrl(rs.getString("snippet_url"));
                  builder.setImageUrl(rs.getString("image_url"));
                  builder.setUrl(rs.getString("url"));
                }
                
                // Join categories information into builder.
                // But why we do not join in sql? Because it'll be difficult to set it in builder.
                sql = "SELECT * from categories WHERE item_id = ?";
                statement = conn.prepareStatement(sql);
                statement.setString(1, itemId);
                rs = statement.executeQuery();
                Set<String> categories = new HashSet<>();
                while (rs.next()) {
                  categories.add(rs.getString("category"));
                }
                builder.setCategories(categories);
                favoriteItems.add(builder.build());
              }
            } catch (SQLException e) {
              e.printStackTrace();
            }
            return favoriteItems;
    }

    @Override
    public String getFullname(String userId) {
        // TODO Auto-generated method stub
//        return null;
        
        String name = "";
        try {
            if (conn == null) {
                return "";
            }
            String sql = "SELECT first_name, last_name from users WHERE user_id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                name += String.join(" ", rs.getString("first_name"), rs.getString("last_name"));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return name;
    }

    @Override
    public boolean verifyLogin(String userId, String password) {
        // TODO Auto-generated method stub
//        return false;
        
        try {
            if (conn == null) {
                return false;
            }

            String sql = "SELECT user_id from users WHERE user_id = ? and password = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            statement.setString(2, password);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

}