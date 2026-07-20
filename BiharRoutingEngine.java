import java.util.*;

// ==========================================
// 1. DATA MODEL CLASSES
// ==========================================

class District {
    String name;
    double latitude;
    double longitude;

    public District(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}

class Highway {
    String sourceDistrict;
    String targetDistrict;
    String highwayName; 
    double distanceKm;
    String highwayType; 
    double averageSpeedKmh;

    public Highway(String sourceDistrict, String targetDistrict, String highwayName, double distanceKm, String highwayType, double averageSpeedKmh) {
        this.sourceDistrict = sourceDistrict;
        this.targetDistrict = targetDistrict;
        this.highwayName = highwayName;
        this.distanceKm = distanceKm;
        this.highwayType = highwayType;
        this.averageSpeedKmh = averageSpeedKmh;
    }

    public double getTravelTimeHours() {
        return this.distanceKm / this.averageSpeedKmh;
    }
}

class PathNode implements Comparable<PathNode> {
    String districtName;
    double costG; 
    double costF; 

    public PathNode(String districtName, double costG, double costF) {
        this.districtName = districtName;
        this.costG = costG;
        this.costF = costF;
    }

    @Override
    public int compareTo(PathNode other) {
        return Double.compare(this.costF, other.costF);
    }
}

// ==========================================
// 2. CORE ROUTING ENGINE & APPLICATION
// ==========================================

public class BiharRoutingEngine {

    private final Map<String, District> registry = new TreeMap<>(); 
    private final Map<String, List<Highway>> graph = new HashMap<>();

    public void addDistrict(String name, double lat, double lon) {
        registry.put(name.toLowerCase(), new District(name, lat, lon));
        graph.putIfAbsent(name.toLowerCase(), new ArrayList<>());
    }

    public void addHighway(String from, String to, String code, double dist, String type) {
        double speed = switch (type) {
            case "Expressway" -> 100.0; 
            case "National Highway" -> 70.0;
            default -> 50.0; 
        };
        
        graph.get(from.toLowerCase()).add(new Highway(from, to, code, dist, type, speed));
        graph.get(to.toLowerCase()).add(new Highway(to, from, code, dist, type, speed)); 
    }

    private double calculateHeuristic(String current, String target, boolean optimizeTime) {
        District d1 = registry.get(current.toLowerCase());
        District d2 = registry.get(target.toLowerCase());
        if (d1 == null || d2 == null) return 0.0;

        final int R = 6371; 
        double latDistance = Math.toRadians(d2.latitude - d1.latitude);
        double lonDistance = Math.toRadians(d2.longitude - d1.longitude);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(d1.latitude)) * Math.cos(Math.toRadians(d2.latitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double absoluteDist = R * c;

        return optimizeTime ? (absoluteDist / 100.0) : absoluteDist; 
    }

    public void printDetailedRoute(String origin, String destination, boolean optimizeTime) {
        String normOrigin = origin.toLowerCase().trim();
        String normDestination = destination.toLowerCase().trim();

        if (!registry.containsKey(normOrigin) || !registry.containsKey(normDestination)) {
            System.out.println("\n❌ Error: One or both district names were not recognized in our registry.");
            return;
        }

        PriorityQueue<PathNode> openSet = new PriorityQueue<>();
        Map<String, Highway> leadingEdgeMap = new HashMap<>(); 
        Map<String, Double> costGMap = new HashMap<>();

        for (String key : registry.keySet()) {
            costGMap.put(key, Double.MAX_VALUE);
        }

        costGMap.put(normOrigin, 0.0);
        openSet.add(new PathNode(normOrigin, 0.0, calculateHeuristic(normOrigin, normDestination, optimizeTime)));

        boolean found = false;
        while (!openSet.isEmpty()) {
            PathNode currentWrapper = openSet.poll();
            String current = currentWrapper.districtName;

            if (current.equals(normDestination)) {
                found = true;
                break;
            }

            for (Highway highway : graph.getOrDefault(current, Collections.emptyList())) {
                String neighbor = highway.targetDistrict.toLowerCase();
                double edgeCost = optimizeTime ? highway.getTravelTimeHours() : highway.distanceKm;
                double tentativeGScore = costGMap.get(current) + edgeCost;

                if (tentativeGScore < costGMap.get(neighbor)) {
                    leadingEdgeMap.put(neighbor, highway);
                    costGMap.put(neighbor, tentativeGScore);
                    double scoreF = tentativeGScore + calculateHeuristic(neighbor, normDestination, optimizeTime);
                    
                    openSet.add(new PathNode(neighbor, tentativeGScore, scoreF));
                }
            }
        }

        if (!found) {
            System.out.println("\n❌ No viable route could be mapped between these locations via our highway grid.");
            return;
        }

        List<Highway> pathEdges = new ArrayList<>();
        String current = normDestination;
        while (leadingEdgeMap.containsKey(current)) {
            Highway edge = leadingEdgeMap.get(current);
            pathEdges.add(0, edge);
            current = edge.sourceDistrict.toLowerCase();
        }

        System.out.println("\n=========================================================================");
        System.out.println("  OFFICIAL TRANSIT ITINERARY: " + registry.get(normOrigin).name + " to " + registry.get(normDestination).name);
        System.out.println("  Optimization Strategy: " + (optimizeTime ? "Fastest Travel Time Profile" : "Shortest Distance Profile"));
        System.out.println("=========================================================================");
        
        double totalDistance = 0;
        double totalTime = 0;
        int step = 1;

        for (Highway edge : pathEdges) {
            String fromName = registry.get(edge.sourceDistrict.toLowerCase()).name;
            String toName = registry.get(edge.targetDistrict.toLowerCase()).name;
            double hours = edge.getTravelTimeHours();
            int mins = (int) Math.round((hours - (int)hours) * 60);
            
            System.out.printf(" Step %d: Leave [%s] ➔ Travel to [%s]\n", step++, fromName, toName);
            System.out.printf("         Route: %s (%s)\n", edge.highwayName, edge.highwayType);
            System.out.printf("         Metrics: %.1f Km | Est. Time: %d hr %d mins\n\n", edge.distanceKm, (int)hours, mins);
            
            totalDistance += edge.distanceKm;
            totalTime += hours;
        }

        int finalHrs = (int) totalTime;
        int finalMins = (int) Math.round((totalTime - finalHrs) * 60);

        System.out.println("-------------------------------------------------------------------------");
        System.out.printf(" SUMMARY PROTOCOL -> Total Distance: %.1f Km | Total Travel Time: %d hr %d mins\n", totalDistance, finalHrs, finalMins);
        System.out.println("=========================================================================");

    }
// Tracking fields for UI Cards
    private double lastTotalDistance = 0.0;
    private double lastTotalTime = 0.0;

    public double getLastTotalDistance() { return lastTotalDistance; }
    public double getLastTotalTime() { return lastTotalTime; }

    // Array accessor for JComboBox
    public String[] getRegisteredDistricts() {
        return registry.values().stream()
                .map(d -> d.name)
                .toArray(String[]::new);
    }

    // Returns formatted string output to print inside the UI text pane
    public String getRouteItinerary(String origin, String destination, boolean optimizeTime) {
        String normOrigin = origin.toLowerCase().trim();
        String normDestination = destination.toLowerCase().trim();

        if (!registry.containsKey(normOrigin) || !registry.containsKey(normDestination)) {
            return "❌ Error: One or both district names were not recognized.";
        }

        PriorityQueue<PathNode> openSet = new PriorityQueue<>();
        Map<String, Highway> leadingEdgeMap = new HashMap<>(); 
        Map<String, Double> costGMap = new HashMap<>();

        for (String key : registry.keySet()) {
            costGMap.put(key, Double.MAX_VALUE);
        }

        costGMap.put(normOrigin, 0.0);
        openSet.add(new PathNode(normOrigin, 0.0, calculateHeuristic(normOrigin, normDestination, optimizeTime)));

        boolean found = false;
        while (!openSet.isEmpty()) {
            PathNode currentWrapper = openSet.poll();
            String current = currentWrapper.districtName;

            if (current.equals(normDestination)) {
                found = true;
                break;
            }

            for (Highway highway : graph.getOrDefault(current, Collections.emptyList())) {
                String neighbor = highway.targetDistrict.toLowerCase();
                double edgeCost = optimizeTime ? highway.getTravelTimeHours() : highway.distanceKm;
                double tentativeGScore = costGMap.get(current) + edgeCost;

                if (tentativeGScore < costGMap.get(neighbor)) {
                    leadingEdgeMap.put(neighbor, highway);
                    costGMap.put(neighbor, tentativeGScore);
                    double scoreF = tentativeGScore + calculateHeuristic(neighbor, normDestination, optimizeTime);
                    openSet.add(new PathNode(neighbor, tentativeGScore, scoreF));
                }
            }
        }

        if (!found) {
            return "❌ No viable route could be mapped between these locations.";
        }

        List<Highway> pathEdges = new ArrayList<>();
        String current = normDestination;
        while (leadingEdgeMap.containsKey(current)) {
            Highway edge = leadingEdgeMap.get(current);
            pathEdges.add(0, edge);
            current = edge.sourceDistrict.toLowerCase();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("OFFICIAL TRANSIT ITINERARY\n");
        sb.append("Origin: ").append(registry.get(normOrigin).name)
          .append("  ➔  Destination: ").append(registry.get(normDestination).name).append("\n");
        sb.append("Mode: ").append(optimizeTime ? "Fastest Travel Time" : "Shortest Physical Distance").append("\n");
        sb.append("---------------------------------------------------------------------------\n\n");

        this.lastTotalDistance = 0;
        this.lastTotalTime = 0;
        int step = 1;

        for (Highway edge : pathEdges) {
            String fromName = registry.get(edge.sourceDistrict.toLowerCase()).name;
            String toName = registry.get(edge.targetDistrict.toLowerCase()).name;
            double hours = edge.getTravelTimeHours();
            int mins = (int) Math.round((hours - (int)hours) * 60);

            sb.append(String.format("[%d] %s ➔ %s\n", step++, fromName, toName));
            sb.append(String.format("    Route: %s (%s)\n", edge.highwayName, edge.highwayType));
            sb.append(String.format("    Segment: %.1f Km | Est: %d hr %d mins\n\n", edge.distanceKm, (int)hours, mins));

            this.lastTotalDistance += edge.distanceKm;
            this.lastTotalTime += hours;
        }

        return sb.toString();
    }
    public static void main(String[] args) {
        BiharRoutingEngine engine = new BiharRoutingEngine();

        // =================================================================
        // THE COMPLETE 38 DISTRICTS DATABASE (With Real Lat/Lon Centers)
        // =================================================================
        engine.addDistrict("Araria", 26.1509, 87.4374);
        engine.addDistrict("Arwal", 25.2434, 84.6677);
        engine.addDistrict("Aurangabad", 24.7573, 84.3752);
        engine.addDistrict("Banka", 24.8844, 86.9242);
        engine.addDistrict("Begusarai", 25.4182, 86.1272);
        engine.addDistrict("Bhagalpur", 25.2425, 87.0145);
        engine.addDistrict("Bhojpur", 25.5647, 84.6677); 
        engine.addDistrict("Buxar", 25.5604, 83.9805);
        engine.addDistrict("Darbhanga", 26.1542, 85.8918);
        engine.addDistrict("East Champaran", 26.6572, 84.9218); 
        engine.addDistrict("Gaya", 24.7955, 85.0007);
        engine.addDistrict("Gopalganj", 26.4687, 84.4422);
        engine.addDistrict("Jamui", 24.9272, 86.2238);
        engine.addDistrict("Jehanabad", 25.2155, 84.9912);
        engine.addDistrict("Kaimur", 25.0445, 83.6115); 
        engine.addDistrict("Katihar", 25.5398, 87.5684);
        engine.addDistrict("Khagaria", 25.5008, 86.5898);
        engine.addDistrict("Kishanganj", 26.0739, 87.9383);
        engine.addDistrict("Lakhisarai", 25.1764, 86.0945);
        engine.addDistrict("Madhepura", 25.9228, 86.7957);
        engine.addDistrict("Madhubani", 26.3496, 86.0700);
        engine.addDistrict("Munger", 25.3748, 86.4735);
        engine.addDistrict("Muzaffarpur", 26.1209, 85.3647);
        engine.addDistrict("Nalanda", 25.1983, 85.5149); 
        engine.addDistrict("Nawada", 24.8894, 85.5399);
        engine.addDistrict("Patna", 25.5941, 85.1376);
        engine.addDistrict("Purnia", 25.7771, 87.4753);
        engine.addDistrict("Rohtas", 24.9392, 84.0169); 
        engine.addDistrict("Saharsa", 25.8835, 86.6006);
        engine.addDistrict("Samastipur", 25.8630, 85.7810);
        engine.addDistrict("Saran", 25.7848, 84.7274); 
        engine.addDistrict("Sheikhpura", 25.1396, 85.8568);
        engine.addDistrict("Sheohar", 26.5186, 85.2930);
        engine.addDistrict("Sitamarhi", 26.5925, 85.4965);
        engine.addDistrict("Siwan", 26.2212, 84.3567);
        engine.addDistrict("Supaul", 26.1136, 86.5944);
        engine.addDistrict("Vaishali", 25.6839, 85.2227); 
        engine.addDistrict("West Champaran", 26.7997, 84.5030); 

        // =================================================================
        // STRUCTURAL REGIONAL ROAD METRIC CONNECTIVITY (Baseline Grid Setup)
        // =================================================================
        
        // --- Golden Quadrilateral / Grand Trunk Corridor System (South Bihar) ---
        engine.addHighway("Kaimur", "Rohtas", "NH-19", 60.0, "National Highway");
        engine.addHighway("Rohtas", "Aurangabad", "NH-19", 40.0, "National Highway");
        engine.addHighway("Aurangabad", "Gaya", "NH-19", 70.0, "National Highway");
        engine.addHighway("Gaya", "Nawada", "NH-31", 60.0, "National Highway");
        engine.addHighway("Nawada", "Jamui", "SH-8", 65.0, "State Highway");
        engine.addHighway("Jamui", "Banka", "SH-25", 60.0, "State Highway");
        
        // --- Patna & Central Hub Conduits ---
        engine.addHighway("Patna", "Jehanabad", "NH-22", 50.0, "National Highway");
        engine.addHighway("Jehanabad", "Gaya", "NH-22", 50.0, "National Highway");
        engine.addHighway("Gaya", "Arwal", "SH-69", 65.0, "State Highway");
        engine.addHighway("Arwal", "Patna", "NH-139", 65.0, "National Highway");
        engine.addHighway("Patna", "Bhojpur", "NH-922", 55.0, "National Highway");
        engine.addHighway("Bhojpur", "Buxar", "NH-922", 70.0, "National Highway");
        engine.addHighway("Bhojpur", "Rohtas", "NH-119", 95.0, "National Highway");
        
        // --- Patna toward East River Lines ---
        engine.addHighway("Patna", "Nalanda", "NH-20", 70.0, "National Highway");
        engine.addHighway("Nalanda", "Nawada", "NH-20", 40.0, "National Highway");
        engine.addHighway("Nalanda", "Sheikhpura", "SH-83", 35.0, "State Highway");
        engine.addHighway("Sheikhpura", "Lakhisarai", "NH-33", 30.0, "National Highway");
        engine.addHighway("Lakhisarai", "Munger", "NH-80", 45.0, "National Highway");
        engine.addHighway("Munger", "Bhagalpur", "NH-80", 65.0, "National Highway");
        engine.addHighway("Bhagalpur", "Banka", "NH-333A", 50.0, "National Highway");
        
        // --- Crossing North (Patna/Vaishali Hub) ---
        engine.addHighway("Patna", "Vaishali", "Mahatma Gandhi Setu / NH-22", 20.0, "National Highway");
        engine.addHighway("Vaishali", "Muzaffarpur", "NH-22", 55.0, "National Highway");
        engine.addHighway("Vaishali", "Saran", "NH-19", 60.0, "National Highway");
        engine.addHighway("Saran", "Siwan", "NH-531", 65.0, "National Highway");
        engine.addHighway("Siwan", "Gopalganj", "NH-531", 35.0, "National Highway");
        engine.addHighway("Gopalganj", "Muzaffarpur", "NH-27", 120.0, "National Highway");
        
        // --- North-Western Limits ---
        engine.addHighway("Gopalganj", "East Champaran", "NH-27 Bridge", 50.0, "National Highway");
        engine.addHighway("East Champaran", "West Champaran", "NH-727", 50.0, "National Highway");
        engine.addHighway("East Champaran", "Muzaffarpur", "NH-28", 80.0, "National Highway");
        engine.addHighway("East Champaran", "Sheohar", "SH-54", 40.0, "State Highway");
        engine.addHighway("Sheohar", "Sitamarhi", "SH-54", 30.0, "State Highway");
        engine.addHighway("Sitamarhi", "Muzaffarpur", "NH-77", 60.0, "National Highway");
        
        // --- East-West Corridor (Mithila / Kosi / Seemanchal Belt via NH-27) ---
        engine.addHighway("Muzaffarpur", "Samastipur", "NH-28", 52.0, "National Highway");
        engine.addHighway("Muzaffarpur", "Darbhanga", "NH-27", 65.0, "National Highway");
        engine.addHighway("Darbhanga", "Madhubani", "NH-527B", 38.0, "National Highway");
        engine.addHighway("Darbhanga", "Samastipur", "SH-50", 40.0, "State Highway");
        engine.addHighway("Darbhanga", "Supaul", "NH-27", 85.0, "National Highway");
        engine.addHighway("Supaul", "Saharsa", "SH-66", 45.0, "State Highway");
        engine.addHighway("Saharsa", "Madhepura", "SH-85", 22.0, "State Highway");
        engine.addHighway("Supaul", "Madhepura", "NH-327E", 50.0, "National Highway");
        engine.addHighway("Madhepura", "Purnia", "NH-107", 75.0, "National Highway");
        
        // --- Deep Far-East Highway Blocks ---
        engine.addHighway("Purnia", "Araria", "NH-57", 42.0, "National Highway");
        engine.addHighway("Araria", "Kishanganj", "NH-327", 70.0, "National Highway");
        engine.addHighway("Purnia", "Kishanganj", "NH-31", 75.0, "National Highway");
        engine.addHighway("Purnia", "Katihar", "NH-131A", 30.0, "National Highway");
        
        // --- Ganga South Highway Connectors ---
        engine.addHighway("Patna", "Begusarai", "NH-31", 125.0, "National Highway");
        engine.addHighway("Begusarai", "Samastipur", "SH-55", 65.0, "State Highway");
        engine.addHighway("Begusarai", "Khagaria", "NH-31", 40.0, "National Highway");
        engine.addHighway("Khagaria", "Bhagalpur", "Vikramshila Bridge", 60.0, "National Highway");
        engine.addHighway("Khagaria", "Saharsa", "SH-95", 70.0, "State Highway");
        engine.addHighway("Khagaria", "Purnia", "NH-31", 115.0, "National Highway");
        engine.addHighway("Lakhisarai", "Begusarai", "Rajendra Setu Link", 45.0, "National Highway");

        // --- Active Expressways ---
        engine.addHighway("Aurangabad", "Patna", "Amas-Darbhanga Exp (Phase 1)", 140.0, "Expressway");
        engine.addHighway("Patna", "Darbhanga", "Amas-Darbhanga Exp (Phase 2)", 105.0, "Expressway");
        engine.addHighway("Patna", "Purnia", "Patna-Purnia Greenfield Expressway", 282.0, "Expressway");

        // =================================================================
        // USER INTERACTION TERMINAL INTERFACE
        // =================================================================
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== BIHAR 38-DISTRICT REGIONAL ROUTING SYSTEM ===");
        System.out.println("Available Districts registered in spatial memory:");
        
        int count = 0;
        for (District d : engine.registry.values()) {
            System.out.printf("%-16s", d.name);
            if (++count % 4 == 0) System.out.println();
        }
        System.out.println("\n-------------------------------------------------------------------------");

        System.out.print("👉 Enter Origin District Name: ");
        String origin = scanner.nextLine().trim();

        System.out.print("👉 Enter Destination District Name: ");
        String destination = scanner.nextLine().trim();

        System.out.println("\nSelect Metric Optimization Strategy:");
        System.out.println(" [1] Shortest Distance Profile (Calculates routes based purely on kilometers)");
        System.out.println(" [2] Fastest Travel Time Profile (Prioritizes new Expressways & High-Speed NH roads)");
        System.out.print("Input Choice (1 or 2): ");
        int choice = scanner.nextInt();
        
        boolean optimizeTime = (choice == 2);

        engine.printDetailedRoute(origin, destination, optimizeTime);
        
        scanner.close();
    // Launch Desktop Dashboard UI
        javax.swing.SwingUtilities.invokeLater(() -> {
            BiharRoutingUI gui = new BiharRoutingUI(engine);
            gui.setVisible(true);
        });
    }
}