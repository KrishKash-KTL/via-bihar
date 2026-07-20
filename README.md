# Bihar Regional Transit Navigator 🗺️

A high-performance pathfinding engine written in Java that maps routes across all **38 districts of Bihar**. The engine utilizes the **A* (A-Star) Search Algorithm** combined with the **Haversine Formula** as an admissible heuristic to calculate optimal travel footprints.

## 🚀 Key Features
* **Full Topology Mapped:** Includes coordinates for all 38 district headquarters across Bihar.
* **Dual Optimization Strategy:** 
  1. **Shortest Distance Profile:** Optimizes route evaluation purely based on structural physical kilometers.
  2. **Fastest Travel Time Profile:** Factors in highway tiers and average operational speeds to leverage Expressways and National Highways.
* **Modern Infrastructure Matrix (2026 Grid):** Integrates major lifelines like NH-27, NH-19, the newly active Amas-Darbhanga Expressway corridors, and planned greenfield networks.

## 🖥️ Graphical User Interface (Executive Dashboard)

The **Bihar Transit Navigator** includes a modern desktop dashboard built using **Java Swing**, providing an intuitive visual interface alongside the traditional terminal-based execution mode.

### Key Features
* **Dark Mode UI**: Designed with a clean modern dark theme for optimal readability.
* **District Selection**: Easily select origin and destination districts via dynamic drop-down menus.
* **Optimization Profiles**: Toggle between:
  * **Shortest Distance Profile**: Finds the most direct geographic route.
  * **Fastest Travel Time Profile**: Prioritizes expressways and major transit corridors.
* **Live Itinerary Panel**: Displays key metrics including total distance, estimated duration, and step-by-step route checkpoints.

---

## 🛠️ Execution & Deployment

### Prerequisites
* Java Development Kit (JDK 17 or higher recommended)

### Step-by-Step Execution
1. **Clone the Repository:**
   ```bash
   git clone [https://github.com/YOUR_USERNAME/bihar-transit-navigator.git](https://github.com/YOUR_USERNAME/bihar-transit-navigator.git)
   cd bihar-transit-navigator

   # Compile the unified source files
javac BiharRoutingEngine.java BiharRoutingUI.java

# Execute the engine
java BiharRoutingEngine
