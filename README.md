# Bihar Regional Transit Navigator 🗺️

A high-performance pathfinding engine written in Java that maps routes across all **38 districts of Bihar**. The engine utilizes the **A* (A-Star) Search Algorithm** combined with the **Haversine Formula** as an admissible heuristic to calculate optimal travel footprints.

## 🚀 Key Features
* **Full Topology Mapped:** Includes coordinates for all 38 district headquarters across Bihar.
* **Dual Optimization Strategy:** 
  1. **Shortest Distance Profile:** Optimizes route evaluation purely based on structural physical kilometers.
  2. **Fastest Travel Time Profile:** Factors in highway tiers and average operational speeds to leverage Expressways and National Highways.
* **Modern Infrastructure Matrix (2026 Grid):** Integrates major lifelines like NH-27, NH-19, the newly active Amas-Darbhanga Expressway corridors, and planned greenfield networks.

## 🛠️ Execution & Deployment

### Prerequisites
* Java Development Kit (JDK 17 or higher recommended)

### Step-by-Step Execution
1. **Clone the Repository:**
   ```bash
   git clone [https://github.com/YOUR_USERNAME/bihar-transit-navigator.git](https://github.com/YOUR_USERNAME/bihar-transit-navigator.git)
   cd bihar-transit-navigator

   # Compile the unified source file
javac BiharRoutingEngine.java

# Execute the engine
java BiharRoutingEngine