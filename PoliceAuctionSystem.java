import java.util.*;

public class PoliceAuctionSystem {
    static Scanner input = new Scanner(System.in);

    static ArrayList<AuctionItem> seizedItems = new ArrayList<>();
    static LinkedList<String> soldLog = new LinkedList<>();
    static Stack<String> actionLog = new Stack<>();
    static Queue<String> pickupQueue = new LinkedList<>();
    static Queue<String> bidQueue = new LinkedList<>();
    static HashMap<String, String> itemStatus = new HashMap<>();
    static PriceBST priceTree = new PriceBST();

    static int auctionLength = 10;
    static AuctionItem[] currentAuction = new AuctionItem[auctionLength];

    public static void main(String[] args) {
        System.out.println("Welcome to the Police Seized Property Auction System");

        addDefaultItems(); //Automatically creates a few AuctionItems

        boolean running = true;
        while (running) {
            System.out.println("\nMenu:\n1. Add New Seized Item\n2. Setup Auction\n3. Place a Bid\n4. View and Sort Items\n5. View Sold Items\n6. View Bid/Pickup Queues\n7. Undo Last Action\n8. Exit");
            int choice = input.nextInt();
            switch (choice) {
                case 1 -> addNewItem();
                case 2 -> setupAuction();
                case 3 -> handleBid();
                case 4 -> viewItems();
                case 5 -> viewSoldItems();
                case 6 -> viewQueues();
                case 7 -> undoAction();
                case 8 -> running = false;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    static void addDefaultItems() {
        AuctionItem[] defaults = {
                new AuctionItem("MacBook Pro 2020", 1400, 85, 900, false, false, "Recovered from fraud case"),
                new AuctionItem("Trek Mountain Bike", 600, 70, 300, false, false, "Abandoned property"),
                new AuctionItem("Rolex Submariner", 8000, 90, 4500, false, false, "Seized during arrest"),
                new AuctionItem("Samsung 65\" QLED TV", 1200, 80, 750, false, false, "Found in vehicle"),
                new AuctionItem("Canon EOS R6 Camera", 2500, 95, 1600, false, false, "Evidence released after case closed")
        };

        for (AuctionItem item : defaults) {
            seizedItems.add(item);
            itemStatus.put(item.getName(), "Available");
            priceTree.insert(item);
            actionLog.push("Added default item: " + item.getName());
        }

        //setupAuction(); //Immediately move them into auction
    }

    static void addNewItem() {
        input.nextLine();
        System.out.print("Enter item name: ");
        String name = input.nextLine();

        System.out.print("Average market value: ");
        double avgValue = input.nextDouble();

        System.out.print("Condition rating (0-100): ");
        int rating = input.nextInt();

        System.out.print("Starting auction price: ");
        double startPrice = input.nextDouble();

        input.nextLine();
        System.out.print("Notes: ");
        String notes = input.nextLine();

        AuctionItem item = new AuctionItem(name, avgValue, rating, startPrice, false, false, notes);
        seizedItems.add(item);
        itemStatus.put(name, "Available");
        priceTree.insert(item);
        actionLog.push("Added item: " + name);
        System.out.println("Item added to police inventory.");
    }

    static void setupAuction() {
        Arrays.fill(currentAuction, null);
        System.out.println("How many items would you like to add to the list (up to " + auctionLength + " items)?");
        int numToAdd = input.nextInt();
        for (int added = 0; added < numToAdd; added++){
            AuctionItem item = seizedItems.get(added);
            if (!item.isForSale() && !item.isSold()) {
                currentAuction[added] = item;
                item.setForSale(true);
                itemStatus.replace(item.getName(), "For Sale");
            }
        }
        System.out.println("Auction setup complete.");
        actionLog.push("Set up auction");
    }

    static void handleBid() {
        input.nextLine();
       /*boolean auctionHasItems = false;
       for (AuctionItem item : currentAuction) {
           if (item != null && item.isForSale() && !item.isSold()) {
               auctionHasItems = true;
               break;
           }
       }*/
        if (currentAuction[0] == null) {
            System.out.println("No items available in the auction. Please set up the auction first.");
            return;
        }

        System.out.println("--- Items currently in auction ---");
        for (int i = 1; i < currentAuction.length + 1; i++){
            try {
                System.out.println(i + ". " + currentAuction[i - 1].getName() + ", $" + currentAuction[i - 1].getStartingPrice());
            }
            catch (Exception e){
                System.out.println(i + ". N/A");
            }
        }
        String name = "";
        while (name.isEmpty()) {
            System.out.print("Enter item name to bid on: ");
            name = input.nextLine().trim();
            if (name.isEmpty()) System.out.println("Item name cannot be blank. Try again.");
        }

        System.out.println("You entered: '" + name + "'");



        for (int i = 0; i < currentAuction.length; i++) {
            AuctionItem item = currentAuction[i];
            if (item != null && item.getName().trim().equalsIgnoreCase(name.trim()) && item.isForSale() && !item.isSold()) {
                System.out.print("Your bid amount: ");
                double bid = input.nextDouble();
                input.nextLine();

                if (bid >= item.getStartingPrice()) {
                    item.setSold(true);
                    item.setForSale(false);
                    currentAuction[i] = null;
                    soldLog.addFirst(item.getName());
                    if (soldLog.size() > 5) soldLog.removeLast();
                    itemStatus.put(item.getName(), "Sold");
                    actionLog.push("Sold item: " + item.getName());
                    bidQueue.offer("Bid on: " + item.getName());
                    pickupQueue.offer("Pickup: " + item.getName());
                    System.out.println("Bid accepted. Item sold!");
                } else {
                    System.out.println("Bid too low.");
                }
                return;
            }
        }
        System.out.println("Item not found or not available.");
    }

    static void viewItems() {
        System.out.println("--- All Seized Items ---");
        for (AuctionItem item : seizedItems) {
            System.out.println(item);
        }

        System.out.println("Sort options:\n1. By Name\n2. By Price\n3. By Rating\n4. View Tree By Price\n5. Skip");
        int sort = input.nextInt();
        switch (sort) {
            case 1 -> {
                seizedItems.sort(Comparator.comparing(AuctionItem::getName));
                System.out.println("--- Sorted by Name ---");
                for (AuctionItem item : seizedItems) System.out.println(item);
            }
            case 2 -> {
                seizedItems.sort(Comparator.comparingDouble(AuctionItem::getStartingPrice));
                System.out.println("--- Sorted by Price ---");
                for (AuctionItem item : seizedItems) System.out.println(item);
            }
            case 3 -> {
                seizedItems.sort(Comparator.comparingInt(AuctionItem::getRating));
                System.out.println("--- Sorted by Rating ---");
                for (AuctionItem item : seizedItems) System.out.println(item);
            }
            case 4 -> priceTree.inOrder();
            case 5 -> {
                break;
            }
        }
    }

    static void viewSoldItems() {
        System.out.println("--- Recently Sold Items ---");
        for (String sold : soldLog) System.out.println(sold);
    }

    static void viewQueues() {
        System.out.println("--- Pending Pickup Queue ---");
        for (String s : pickupQueue) System.out.println(s);

        System.out.println("--- Pending Bid Queue ---");
        for (String b : bidQueue) System.out.println(b);
    }

    static void undoAction() {
        if (!actionLog.isEmpty()) System.out.println("Undo: " + actionLog.pop());
        else System.out.println("No actions to undo.");
    }
}

class PriceBST {
    class Node {
        AuctionItem item;
        Node left, right;
        Node(AuctionItem item) { this.item = item; }
    }

    private Node root;

    public void insert(AuctionItem item) {
        root = insertRec(root, item);
    }

    private Node insertRec(Node root, AuctionItem item) {
        if (root == null) return new Node(item);
        if (item.getStartingPrice() < root.item.getStartingPrice())
            root.left = insertRec(root.left, item);
        else
            root.right = insertRec(root.right, item);
        return root;
    }

    public void inOrder() {
        System.out.println("--- Items Sorted by Price (BST InOrder) ---");
        inOrderRec(root);
    }

    private void inOrderRec(Node node) {
        if (node != null) {
            inOrderRec(node.left);
            System.out.println(node.item);
            inOrderRec(node.right);
        }
    }
}

class AuctionItem {
    private String name;
    private double avgRetailPrice;
    private int rating;
    private double startingPrice;
    private boolean isForSale;
    private boolean isSold;
    private String notes;

    public AuctionItem(String name, double avgRetailPrice, int rating, double startingPrice, boolean isForSale, boolean isSold, String notes) {
        this.name = name;
        this.avgRetailPrice = avgRetailPrice;
        this.rating = rating;
        this.startingPrice = startingPrice;
        this.isForSale = isForSale;
        this.isSold = isSold;
        this.notes = notes;
    }

    public String getName() { return name; }
    public double getAvgRetailPrice() { return avgRetailPrice; }
    public int getRating() { return rating; }
    public double getStartingPrice() { return startingPrice; }
    public boolean isForSale() { return isForSale; }
    public void setForSale(boolean forSale) { isForSale = forSale; }
    public boolean isSold() { return isSold; }
    public void setSold(boolean sold) { isSold = sold; }

    public String toString() {
        return name + " | Rating: " + rating + "/100 | Price: $" + startingPrice +
                " | Status: " + (isSold ? "Sold" : (isForSale ? "For Sale" : "Inventory")) +
                " | Notes: " + notes;
    }
}
