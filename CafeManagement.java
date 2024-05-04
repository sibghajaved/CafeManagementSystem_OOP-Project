import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CafeManagement extends Application {

    private Map<String, User> userCredentials; 
    private Connection connection;

    public static void main(String[] args) {
        launch(args);
    }
   

    @Override
    public void start(Stage primaryStage) {
        
        userCredentials = new HashMap<>();
        userCredentials.put("user1", new User("user1", "pass1")); 
        LoginForm loginForm = new LoginForm();
        loginForm.start(primaryStage);
    }
    class DatabaseConnector {

    	public static Connection connect() {
            Connection connection = null;
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                String url = "jdbc:mysql://localhost:3306/cafedb";
                String username = "root";
                String password = "";
                connection = DriverManager.getConnection(url, username, password);
                System.out.println("Connected to the database");
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }
            return connection;
        }

       
        public static boolean doesUsernameExist(Connection connection, String username) {
            try {
                String query = "SELECT * FROM users WHERE username = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, username);

                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        return resultSet.next();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        
        public static boolean createUser(Connection connection, String username, String password) {
            try {
                if (doesUsernameExist(connection, username)) {
                    System.out.println("Username already exists. Please choose another username.");
                    return false;
                }

                String query = "INSERT INTO users (username, password) VALUES (?, ?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, username);
                    preparedStatement.setString(2, password);

                    int affectedRows = preparedStatement.executeUpdate();
                    return affectedRows > 0;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

       
        public static boolean authenticateUser(Connection connection, String username, String password) {
            try {
                String query = "SELECT * FROM users WHERE username = ? AND password = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, username);
                    preparedStatement.setString(2, password);

                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        return resultSet.next();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        
        public static boolean createOrder(Connection connection, String itemName, double itemPrice, String customerName, double paymentAmount, double balance) {
        	
        	 try {
        
        	        if (customerName == null || customerName.trim().isEmpty()) {
        	            System.out.println("Customer name cannot be null or empty.");
        	            return false;
        	        }

        	        String query = "INSERT INTO orders (item_name, item_price, customer_name, payment_amount, balance) VALUES (?, ?, ?, ?, ?)";
        	        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
        	            preparedStatement.setString(1, itemName);
        	            preparedStatement.setDouble(2, itemPrice);
        	            preparedStatement.setString(3, customerName);
        	            preparedStatement.setDouble(4, paymentAmount);
        	            preparedStatement.setDouble(5, balance);

        	            int affectedRows = preparedStatement.executeUpdate();
        	            return affectedRows > 0;
        	        }
        	    } catch (SQLException e) {
        	        e.printStackTrace();
        	        return false;
        	    }
        }
   
        	 
        public static List<Order> getAllOrders(Connection connection) {
            List<Order> orders = new ArrayList<>();
            try {
                String query = "SELECT * FROM orders";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                     ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        Order order = new Order(
                                resultSet.getInt("order_id"),
                                resultSet.getString("item_name"),
                                resultSet.getDouble("item_price"),
                                resultSet.getTimestamp("order_date").toLocalDateTime(),
                                resultSet.getString("customer_name"),
                                resultSet.getDouble("payment_amount"),
                                resultSet.getDouble("balance")
                        );
                        orders.add(order);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return orders;
        }

    }

    public class User {
        private String username;
        private String password;

        public User(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }

  
    class LoginForm extends Application {
    	  private String loggedInUsername;
    	  private CafeInterface.placeOrder placeOrderInstance;
    	 
    	  public void setLoggedInUsername(String loggedInUsername) {
              this.loggedInUsername = loggedInUsername;
          }
        @Override
        public void start(Stage primaryStage) {
    
        	 connection = DatabaseConnector.connect();
        	 placeOrderInstance = new CafeInterface.placeOrder(); 
        	
            primaryStage.setTitle("Login");

            // Create layout components
            StackPane root = new StackPane();
            VBox vbox = new VBox(10);
            vbox.setAlignment(Pos.CENTER);
            vbox.setPadding(new Insets(20));

            // Background image
            Image backgroundImage = new Image("login.jpg"); 
            BackgroundImage background = new BackgroundImage(
                    backgroundImage, null, null, null,
                    new BackgroundSize(200, 200, false, false, true, true) 
            );
            root.setBackground(new Background(background));

            // Create form components
            Label titleLabel = new Label("Login");
            titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
            Label usernameLabel = new Label("Email Address:");
            TextField usernameField = new TextField();
            
            usernameField.setStyle("-fx-font-size: 10;");
            usernameField.setPrefWidth(150); 

            Label passwordLabel = new Label("Password:");
            PasswordField passwordField = new PasswordField();
            passwordField.setPrefWidth(150); 
            Button loginButton = new Button("Login");
            loginButton.setStyle(
                "-fx-background-color: #4CAF50;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;"
            );
            loginButton.setMinWidth(100);
            Text signupMessage = new Text("Not a member? Sign up here");
            signupMessage.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
            Hyperlink signupLink = new Hyperlink();
            signupLink.setGraphic(signupMessage);

            signupLink.setOnAction(e -> {
                SignUpForm signUpForm = new SignUpForm();
                signUpForm.start(new Stage());
            });

            
            vbox.getChildren().addAll(
                    titleLabel, usernameLabel, usernameField,
                    passwordLabel, passwordField, loginButton,
                    new Label(), signupLink
            );

          
            vbox.setStyle("-fx-background-color: grey;");
            vbox.setMaxSize(300, 500);        
            root.getChildren().add(vbox);            
            loginButton.setOnAction(e -> {
                String username = usernameField.getText();
                String password = passwordField.getText();

                if (DatabaseConnector.authenticateUser(connection, username, password)) {
                
                	placeOrderInstance.setLoggedInUsername(username);
                    primaryStage.close(); 

                  
                    Stage cafeStage = new Stage();
                    CafeInterface cafeInterface = new CafeInterface();
                    cafeInterface.setPlaceOrderInstance(placeOrderInstance); 
                    cafeInterface.start(cafeStage);
                    
                   
                    } 
                    
                 else {
                   
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Invalid username or password.");
                    alert.showAndWait();
                }
            });
           
           
            Scene scene = new Scene(root, 1500, 700);
            primaryStage.setScene(scene);
            primaryStage.show();
          
        }
      
        @Override
        public void stop() {
            
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                    System.out.println("Database connection closed");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

       
    }

    
    class SignUpForm extends Application {

        @Override
        public void start(Stage primaryStage) {
        	connection = DatabaseConnector.connect();

            primaryStage.setTitle("Sign Up");

           
            StackPane root = new StackPane();
            VBox vbox = new VBox(10);
            vbox.setAlignment(Pos.CENTER);
            vbox.setPadding(new Insets(20));

         
            Image backgroundImage = new Image("login.jpg"); 
            BackgroundImage background = new BackgroundImage(
                    backgroundImage, null, null, null,
                    new BackgroundSize(200, 200, false, false, true, true)
            );
            root.setBackground(new Background(background));

            Label titleLabel = new Label("Sign Up");
            titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
            Label usernameLabel = new Label("Email Address:");
            TextField usernameField = new TextField();
            usernameField.setStyle("-fx-font-size: 10;");
            usernameField.setPrefWidth(150); // Set the preferred width

            Label passwordLabel = new Label("Password:");
            PasswordField passwordField = new PasswordField();
            passwordField.setPrefWidth(150); // Set the preferred width
            
            
            Button signupButton = new Button("Sign up");
            signupButton.setStyle(
                "-fx-background-color: #4CAF50;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;"
            );
            signupButton.setMinWidth(100);
          
            vbox.getChildren().addAll(
                    titleLabel, usernameLabel, usernameField,
                    passwordLabel, passwordField, signupButton
            );

          
            vbox.setStyle("-fx-background-color: grey;");
            vbox.setMaxSize(300, 500);

            
            root.getChildren().add(vbox);
           
            signupButton.setOnAction(e -> {
               
                String username = usernameField.getText();
                String password = passwordField.getText();

                if (username.isEmpty() || password.isEmpty()) {
                  
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Username and password cannot be empty.");
                    alert.showAndWait();
                }
                    
                    else if (DatabaseConnector.doesUsernameExist(connection, username)) {
                    	 Alert alert = new Alert(Alert.AlertType.ERROR);
                         alert.setTitle("Error");
                         alert.setHeaderText(null);
                         alert.setContentText("Username is already taken. Please choose another.");
                         alert.showAndWait();
                    } else {
                        DatabaseConnector.createUser(connection, username, password);
                        primaryStage.close();
                    }
                });
        

            Scene scene = new Scene(root, 1500, 700);

            
            primaryStage.setScene(scene);
            primaryStage.show();
        }
    }
    
    public static class Order {
        private int orderId;
        private String itemName;
        private double itemPrice;
        private LocalDateTime orderDate;
        private String customerName;
        private double paymentAmount;
        private double balance;

        public Order(int orderId, String itemName, double itemPrice, LocalDateTime orderDate, String customerName, double paymentAmount, double balance) {
            this.orderId = orderId;
            this.itemName = itemName;
            this.itemPrice = itemPrice;
            this.orderDate = orderDate;
            this.customerName = customerName;
            this.paymentAmount = paymentAmount;
            this.balance = balance;
        }

      
        public int getOrderId() {
            return orderId;
        }

        public String getItemName() {
            return itemName;
        }

        public double getItemPrice() {
            return itemPrice;
        }

        public LocalDateTime getOrderDate() {
            return orderDate;
        }

        public String getCustomerName() {
            return customerName;
        }

        public double getPaymentAmount() {
            return paymentAmount;
        }

        public double getBalance() {
            return balance;
        }
    }
}



class CafeInterface extends Application 
{
	private placeOrder placeOrderInstance;

   
	 public void setPlaceOrderInstance(placeOrder placeOrderInstance) {
	        this.placeOrderInstance = placeOrderInstance;
	    }
    @Override
    public void start(Stage primaryStage) 
    {
        primaryStage.setTitle("Bean Cafe");

        // Create main layout
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(2);
        grid.setHgap(150);
        
        int maxRows = 3; 
        int maxCols = 5; 
        
        ColumnConstraints column = new ColumnConstraints();
        column.setPercentWidth(100 / (maxCols + 6)); 
        for (int i = 0; i < maxCols + 6; i++) 
        {
            grid.getColumnConstraints().add(column);
        }
        
        RowConstraints titleRow = new RowConstraints();
        titleRow.setPercentHeight(3); 
        RowConstraints secondaryTitleRow = new RowConstraints();
        secondaryTitleRow.setPercentHeight(2);
        
       
        RowConstraints menuItemRows = new RowConstraints();
        menuItemRows.setPercentHeight(30); 
        grid.getRowConstraints().addAll(titleRow, secondaryTitleRow, menuItemRows, menuItemRows, menuItemRows);

        
        

        // Set background image       
        Image backgroundImage = new Image("interface.jpg"); // Replace with your image path
        BackgroundImage background = new BackgroundImage
        		(
                backgroundImage, null, null, null,
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
        );
        grid.setBackground(new Background(background));                     
        
        Label titleLabel = new Label("Bean Dream");
        titleLabel.setStyle("-fx-font-size: 34px; -fx-font-weight: bold; -fx-text-fill: red; -fx-font-family: 'Times New Roman';");
        GridPane.setColumnSpan(titleLabel, 6); // Set the column span to match the number of menu items
        grid.add(titleLabel, 5, 0);

        // Secondary line label
        Label secondaryLabel = new Label("The place of your dreams");
        secondaryLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white; -fx-font-family: 'Times New Roman';");
        GridPane.setColumnSpan(secondaryLabel, 6); // Set the column span to match the number of menu items
        grid.add(secondaryLabel, 5, 1);
        Label[] menuNames = 
        	{
        	    new Label("Espresso"),
        	    new Label("Americano"),
        	    new Label("Macchiato"),
        	    new Label("Cappuccino"),
        	    new Label("Latte"),
        	    new Label("Cheesecake"),
        	    new Label("Fruit tart"),
        	    new Label("Tiramisu"),
        	    new Label("Cupcake"),
        	    new Label("Gulab Jamun(3 pcs)"),
        	    new Label("Pancakes"),
        	    new Label("French Toast"),
        	    new Label("Club sandwich"),
        	    new Label("Greek Salad"),
        	    new Label("Alfredo Pasta"),
        	};

        	Label[] menuPrices = 
        		{
        	    new Label("Rs.150"),
        	    new Label("Rs.200"),
        	    new Label("Rs.200"),
        	    new Label("Rs.220"),
        	    new Label("Rs.250"),
        	    new Label("Rs.350"),
        	    new Label("Rs.300"),
        	    new Label("Rs.320"),
        	    new Label("Rs.150"),
        	    new Label("Rs.180"),
        	    new Label("Rs.200"),
        	    new Label("Rs.200"),
        	    new Label("Rs.380"),
        	    new Label("Rs.320"),
        	    new Label("Rs.450"),
        	};

        	Label[] menuDescriptions = 
        		{
        	    new Label("A bold and rich shot of concentrated coffee, an energizing delight."),
        	    new Label("An invigorating blend of espresso and hot water, perfect for a smooth sip."),
        	    new Label("A harmonious mix of espresso and velvety steamed milk, a treat for the senses."),
        	    new Label("Indulge in the perfect balance of espresso, steamed milk, and foam."),
        	    new Label("A creamy concoction of espresso and steamed milk, topped with a touch of foam."),
        	    new Label("A heavenly dessert with a velvety texture and a delightful blend of flavors."),
        	    new Label("A burst of freshness with a medley of seasonal fruits atop a buttery crust."),
        	    new Label("An exquisite Italian dessert, layering coffee-soaked ladyfingers and mascarpone."),
        	    new Label("A mini delight, moist and sweet, a treat for your taste buds."),
        	    new Label("Soft, syrup-soaked dough balls, a traditional Indian delight."),
        	    new Label("Soft, fluffy pancakes, a classic breakfast choice topped with maple syrup."),
        	    new Label("Savor the crispy exterior and soft interior of this breakfast classic."),
        	    new Label("A stacked ensemble of layers, perfect for a hearty meal."),
        	    new Label("A refreshing salad with crisp veggies, olives, and creamy feta."),
        	    new Label("Silky pasta coated in a creamy and flavorful Alfredo sauce."),
        	    
        	   
        	};
        	
        	for (Label label : menuNames) 
        	{
        	    label.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        	}

        	for (Label label : menuPrices) 
        	{
        	    label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: red;");
        	}

        	for (Label label : menuDescriptions) 
        	{
        	    label.setStyle("-fx-font-size: 12px; -fx-text-fill: white ; -fx-wrap-text: true; -fx-max-width: 150;");
        	 }
        	
        Image espressoImage = new Image("img1.jpg");
        Image americanoImage = new Image("imgg2.jpg");
        Image macchiatoImage = new Image("img3.jpg");
        Image cappuccinoImage = new Image("img4.jpeg");
        Image latteImage = new Image("img5.jpg");
        Image cheesecakeImage = new Image("img6.jpg");
        Image fruittartImage = new Image("img7.jpg");
        Image tiramisuImage = new Image("img8.jpg");
        Image cupcakeImage = new Image("img9.jpg");
        Image gulabjamunImage = new Image("img10.jpg");
        Image pancakesImage = new Image("img11.jpeg");
        Image frenchtoastImage = new Image("img12.jpg");
        Image clubsandwichImage = new Image("img13.jpg");
        Image greeksaladImage = new Image("img14.jpg");
        Image alfredopastaImage = new Image("img15.jpg");
       
        // ... Add more images for other menu items
        ImageView espressoImageView = new ImageView(espressoImage);
        ImageView americanoImageView = new ImageView(americanoImage);
        ImageView macchiatoImageView = new ImageView(macchiatoImage);
        ImageView cappuccinoImageView = new ImageView(cappuccinoImage);
        ImageView latteImageView = new ImageView(latteImage);
        ImageView cheesecakeImageView = new ImageView(cheesecakeImage);
        ImageView fruittartImageView = new ImageView(fruittartImage);
        ImageView tiramisuImageView = new ImageView(tiramisuImage);
        ImageView cupcakeImageView = new ImageView(cupcakeImage);
        ImageView gulabjamunImageView = new ImageView(gulabjamunImage);
        ImageView pancakesImageView = new ImageView(pancakesImage);
        ImageView frenchtoastImageView = new ImageView(frenchtoastImage);
        ImageView clubsandwichImageView = new ImageView(clubsandwichImage);
        ImageView greeksaladImageView = new ImageView(greeksaladImage);
        ImageView alfredopastaImageView = new ImageView(alfredopastaImage);

        double imageWidth = 150; 
        double imageHeight = 150; 
        espressoImageView.setFitWidth(imageWidth);
        espressoImageView.setFitHeight(imageHeight);
        americanoImageView.setFitWidth(imageWidth);
        americanoImageView.setFitHeight(imageHeight);
        macchiatoImageView.setFitWidth(imageWidth);
        macchiatoImageView.setFitHeight(imageHeight);
        cappuccinoImageView.setFitWidth(imageWidth);
        cappuccinoImageView.setFitHeight(imageHeight);
        latteImageView.setFitWidth(imageWidth);
        latteImageView.setFitHeight(imageHeight);
        cheesecakeImageView.setFitWidth(imageWidth);
        cheesecakeImageView.setFitHeight(imageHeight);
        fruittartImageView.setFitWidth(imageWidth);
        fruittartImageView.setFitHeight(imageHeight);
        tiramisuImageView.setFitWidth(imageWidth);
        tiramisuImageView.setFitHeight(imageHeight);
        cupcakeImageView.setFitWidth(imageWidth);
        cupcakeImageView.setFitHeight(imageHeight);
        gulabjamunImageView.setFitWidth(imageWidth);
        gulabjamunImageView.setFitHeight(imageHeight);
        pancakesImageView.setFitWidth(imageWidth);
        pancakesImageView.setFitHeight(imageHeight);
        frenchtoastImageView.setFitWidth(imageWidth);
        frenchtoastImageView.setFitHeight(imageHeight);
        clubsandwichImageView.setFitWidth(imageWidth);
        clubsandwichImageView.setFitHeight(imageHeight);
        greeksaladImageView.setFitWidth(imageWidth);
        greeksaladImageView.setFitHeight(imageHeight);
        alfredopastaImageView.setFitWidth(imageWidth);
        alfredopastaImageView.setFitHeight(imageHeight);
       
        // ... Set fitWidth and fitHeight for other ImageViews

        

        Button placeOrderButton = new Button("Place Order");
        placeOrderButton.setPrefWidth(250); // Set your desired width
        placeOrderButton.setPrefHeight(50); // Set your desired height
        placeOrderButton.setStyle
        ( "-fx-font-size: 16px; " +
        	    "-fx-font-weight: bold; " +
        	    "-fx-text-fill: white; " +
        	    "-fx-background-color: #4CAF50;" + // Set your desired background color
        	    "-fx-border-color: #45a049;" +    // Set your desired border color
        	    "-fx-border-width: 2px;");
        	              // Set your desired border width
        int currentMenuItem = 0;

        for (int row = 0; row < maxRows; row++) 
        {
            for (int col = 0; col < maxCols; col++) 
            {
                if (currentMenuItem >= menuNames.length) 
                {
                    break;
                }

                int colIndex = col + 3;
                int rowIndex = row + 2;

                ImageView currentImageView = null;
                switch (currentMenuItem) 
                {
                    case 0:
                        currentImageView = espressoImageView;
                        break;
                    case 1:
                        currentImageView = americanoImageView;
                        break;
                    case 2:
                        currentImageView = macchiatoImageView;
                        break;
                    case 3:
                        currentImageView = cappuccinoImageView;
                        break;
                    case 4:
                        currentImageView = latteImageView;
                        break;
                    case 5:
                        currentImageView = cheesecakeImageView;
                        break;
                    case 6:
                        currentImageView = fruittartImageView;
                        break;
                    case 7:
                        currentImageView = tiramisuImageView;
                        break;
                    case 8:
                        currentImageView = cupcakeImageView;
                        break;
                    case 9:
                        currentImageView = gulabjamunImageView;
                        break;
                    case 10:
                        currentImageView = pancakesImageView;
                        break;
                    case 11:
                        currentImageView = frenchtoastImageView;
                        break;
                    case 12:
                        currentImageView = clubsandwichImageView;
                        break;
                    case 13:
                        currentImageView = greeksaladImageView;
                        break;
                    case 14:
                        currentImageView = alfredopastaImageView;
                        break;
                    // Add more cases for other menu items
                }

                grid.add(new VBox(currentImageView, menuNames[currentMenuItem], menuPrices[currentMenuItem], menuDescriptions[currentMenuItem]), colIndex, rowIndex);
                currentMenuItem++;
            }
        }
        grid.add(placeOrderButton, 5, 8);

        // Event handler for place order button
        placeOrderButton.setOnAction(e -> {
            // Launch the order management system (your existing code)
            placeOrder orderSystem = new placeOrder();
            Stage orderStage = new Stage(); // Create a new stage for the order system
            orderSystem.start(orderStage);
        });
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(grid);
        
        Scene scene = new Scene(scrollPane, 500, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    
   
}



public static class placeOrder extends Application 
{
    private ListView<String> menuListView;
    private ListView<String> orderListView;
    private ListView<String> subItemsListView;
    
    private Map<String, String[]> subItems;
    private Map<String, Double> menu;
    private TextField totalField;
    private TextField balanceField;
    private String loggedInUsername; 


    @Override
    public void start(Stage primaryStage) 
    {
        primaryStage.setTitle("Bean Dream");

        menuListView = new ListView<>();
        orderListView = new ListView<>();
        subItemsListView = new ListView<>();
        menu = new HashMap<>();
        
   
        

        menu.put("Espresso", 150.0);
        menu.put("Americano", 200.0);
        menu.put("Macchiato", 200.0);
        menu.put("Cappuccino",220.0);
        menu.put("Latte", 250.0);

        menu.put("Cheesecake", 350.0);
        menu.put("Fruit tart", 300.0);
        menu.put("Tiramisu", 320.0);
        menu.put("Cupcake", 150.0);
        menu.put("Gulab Jamun(3 pcs)", 180.0 );

        menu.put("Pancakes", 200.0);
        menu.put("French Toast", 200.0);
        menu.put("Club sandwich", 380.0);
        menu.put("Greek Salad", 320.0);
        menu.put("Alfredo Pasta", 450.0);

        // Add menu items
       
       
       
        subItems = new HashMap<>();
        subItems.put("Drinks", new String[]{"Espresso", "Americano", "Macchiato", "Cappuccino", "Latte"});
        subItems.put("Desserts", new String[]{"Cheesecake", "Fruit tart", "Tiramisu", "Cupcake", "Gulab Jamun"});
        subItems.put("Snacks", new String[]{"Pancakes", "French Toast", "Club sandwich", "Greek Salad", "Alfredo Pasta"});

        
        menuListView.getItems().addAll("Drinks", "Desserts", "Snacks");
        
        menuListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && subItems.containsKey(newValue)) {
                subItemsListView.getItems().clear();
                subItemsListView.getItems().addAll(subItems.get(newValue));
            }
        });
        
      
       
        
        Button addButton = new Button("Add Item");
        addButton.setStyle("-fx-background-color: green; -fx-text-fil:white; -fx-font-weight:bold;");
        Button removeButton = new Button("Remove Item");
        removeButton.setStyle("-fx-background-color: red; -fx-text-fil:white; -fx-font-weight:bold;");
        Button calculateButton = new Button("Calculate Total");
        calculateButton.setStyle("-fx-background-color: orange; -fx-text-fil:white; -fx-font-weight:bold;");

        totalField = new TextField();
        balanceField = new TextField();

        // Layout
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(5);
        grid.setHgap(5);
        Label label1 = new Label("Menu:");
        label1.setStyle("-fx-font-family: Arial; -fx-font-size: 30px; -fx-font-weight: bolder; -fx-text-fill: white;");
        Label label2 = new Label("Subitems:");
        label2.setStyle("-fx-font-family: Times New Roman; -fx-font-size: 30px; -fx-font-weight: bolder; -fx-text-fill: white;");
        Label label3 = new Label("Your Order:");
        label3.setStyle("-fx-font-family: Times New Roman; -fx-font-size: 30px; -fx-font-weight: bolder; -fx-text-fill: white;");
        Label label4 = new Label("Total Cost:");
        label4.setStyle("-fx-font-family: Times New Roman; -fx-font-size: 30px; -fx-font-weight: bolder; -fx-text-fill: white;");
        Label label5 = new Label("Balance: ");
        label5.setStyle("-fx-font-family: Times New Roman; -fx-font-size: 30px; -fx-font-weight: bolder; -fx-text-fill: white;");
        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: red; -fx-text-fil:white; -fx-font-weight:bold;");
        grid.add(logoutButton, 3, 7);

        logoutButton.setOnAction(e -> {
            // When the logout button is clicked, go back to the login page
            CafeManagement cafeManagementInstance = new CafeManagement();
            CafeManagement.LoginForm loginPage = cafeManagementInstance.new LoginForm();
            loginPage.start(primaryStage);
        });



        grid.add(label1, 0, 0);
        grid.add(menuListView, 0, 1);
        grid.add(label2,  1, 0);
        grid.add(subItemsListView, 1, 1);
        grid.add(label3, 2, 0);  
        grid.add(orderListView, 2, 1);        
        grid.add(addButton, 0, 2);
        grid.add(removeButton, 1, 2);
        grid.add(label4, 0, 3);
        grid.add(totalField, 1, 3);
        grid.add(label5, 0, 4);
        grid.add(balanceField, 1, 4);
        grid.add(calculateButton, 2 , 2 );
        grid.setStyle("-fx-background-image: url('img3.jpg'); -fx-background-size: cover;");
        grid.setAlignment(Pos.CENTER);

        // Event handlers
        addButton.setOnAction(e -> addSelectedItem());
        removeButton.setOnAction(e -> removeSelectedItem());
        calculateButton.setOnAction(e -> calculateTotal());

        Scene scene = new Scene(grid, 1500, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void addSelectedItem() {
        String selectedItem = subItemsListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            orderListView.getItems().add(selectedItem);
        }
    }

    private void removeSelectedItem() 
    {
        String selectedItem = orderListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) 
        {
            orderListView.getItems().remove(selectedItem);
        }
    }
    
    
    private void calculateTotal() {
        // Calculate the total cost of the selected items
        double total = orderListView.getItems().stream()
                .filter(menu::containsKey)  // Filter out items not in the menu
                .mapToDouble(menu::get)
                .sum();

        totalField.setText(String.format("%.2f", total));

       
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Enter Payment Amount:");
        dialog.showAndWait().ifPresent(userPayment -> {
            double payment = Double.parseDouble(userPayment);

           
            if (payment < total) {
               
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Error");
                alert.setContentText("Insufficient Payment!");
                alert.showAndWait();
            } else {
              
                double balance = payment - total;
                balanceField.setText(String.format("%.2f", balance));

                
                TextInputDialog nameDialog = new TextInputDialog();
                nameDialog.setHeaderText("Enter Customer Name:");
                nameDialog.showAndWait().ifPresent(customerName -> {
                    if (!customerName.trim().isEmpty()) {  
                        
                        boolean orderSaved = saveOrderToDatabase(orderListView.getItems(), total, payment, balance, customerName);

                        if (orderSaved) {
                            System.out.println("Order saved to the database successfully!");
                        } else {
                            System.out.println("Failed to save the order to the database.");
                        }

                      
                        Alert receiptAlert = new Alert(Alert.AlertType.INFORMATION);
                        receiptAlert.setHeaderText("Receipt");
                        receiptAlert.setContentText(String.format("Total: %.2f\nPayment: %.2f\nBalance: %.2f", total, payment, balance));
                        receiptAlert.showAndWait();
                    } else {
                        
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setHeaderText("Error");
                        alert.setContentText("Customer name cannot be empty!");
                        alert.showAndWait();
                    }
                });
            }
        });
    }


    public void setLoggedInUsername(String loggedInUsername) {
        this.loggedInUsername = loggedInUsername;
    }
    
    private boolean saveOrderToDatabase(ObservableList<String> items, double total, double payment, double balance, String customerName) {
        Connection connection = CafeManagement.DatabaseConnector.connect();

        if (connection != null) {
            
            boolean orderCreated = CafeManagement.DatabaseConnector.createOrder(
                    connection,
                    String.join(", ", items),
                    total,
                    customerName, 
                    payment,
                    balance
            );

            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return orderCreated;
        }

        return false;
    }
}
}
