# BREATHE – A Respiratory System Simulator for Mechanical Ventilator Testing and Training

BREATHE is a simulation software based on [Kitware's Pulse Physiology Engine](https://gitlab.kitware.com/physiology/engine), designed to test mechanical ventilators and provide training for students on how to operate these devices.  
The application includes both a Swing interface for testing external ventilators and a web-based interface.

## Components Overview
- **Engine**: The core simulation engine responsible for simulation.
- **Swing Interface**: Built with Java Swing for local testing of external ventilators.
- **Web Interface**: A web app designed specifically for training purposes.
- **ZeroMQ Client**: An external client simulating ventilator behavior, enabling seamless communication between BREATHE and external ventilator systems.



## Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/GionathaPirola/BREATHE.git
   cd BREATHE
   
2. Import the Maven dependencies for each project component (engine, swingGUI, web, zeroMQ) by running `mvn install`
3. Launch the application by running the main class of either the Swing or WebApp project, depending on the interface you wish to use.  
If you successfully launch the web app, you can access it at [http://localhost:8080](http://localhost:8080).

## Web App Usage

The view is composed of the following elements:

- **Patient Information**: Displays all information about the patient.
- **Conditions**: Shows all conditions applied to the patient at the start of the simulation.
- **Actions**: Lists all actions that can be applied to the patient during the simulation after it has started.
- **Ventilators**: Contains all ventilators that can be connected during the simulation. Unlike the Swing interface, which supports a ZeroMQ server for external ventilators, the web app focuses on internal ventilator configurations. Users can select from various available ventilators to simulate different mechanical ventilation settings.
- **Output Display**: Displays all output information related to the running simulation.
- **Scenario**:  Allows users to create scenarios by selecting patient states (from `breathe.engine/states/`) and adding actions to be applied at specific times. The scenario file is exported and can be downloaded locally.

The simulation can be started using the following methods:

1. **Standard Simulation**: After configuring the patient parameters and conditions, initiate the simulation to begin the stabilization process. This may take a few minutes, depending on the computer's performance. Once stabilization is complete, the patient state will be exported to the `breathe.engine/states/` folder, and the simulation will start immediately.
2. **From Uploaded File**: Upload a states file from a local folder directly through the web interface to start the simulation immediately. This feature allows for quick resumption of previous states, enabling users to continue their training from a specific point.
3. **From Scenario File**: Upload a scenario file from a local folder.  The simulation will start immediately because scenario files use a states file.

The simulation can be exported at any time during its execution, and state files can be stored locally.



## Swing Interface Usage

The view is composed of the following elements:

- **Patient Information**: Displays all information about the patient.
- **Conditions**: Shows all conditions applied to the patient at the start of the simulation.
- **Actions**: Lists all actions that can be applied to the patient during the simulation after it has started.
- **Ventilators**: Contains all ventilators that can be connected during the simulation. In addition to the standard ventilator, there is also a ZeroMQ server representing an external ventilator. When connected, the server will open and search for a connection (see ZeroMQ Client usage).
- **Output Panel**: Displays all output information related to the running simulation.
- **Log Output**: Similar to the output panel, but provides the information about the simulation in written form.
- **Scenario**: Allows users to create scenarios by selecting patient states (from `breathe.engine/states/`) and adding actions to be applied at specific times. The scenario file is exported to the `breathe.engine/scenario/exported/` folder.
The simulation can be started using three methods:

1. **Standard Simulation**: After configuring the patient parameters and conditions, initiate the simulation to begin the stabilization process. This may take a few minutes, depending on the computer's performance. Once stabilization is complete, the patient state will be exported to the `breathe.engine/states/` folder, and the simulation will start immediately.
2. **From File**: Select a states file. This feature allows for quick resumption of previous states, enabling users to continue their training from a specific point.
3. **From Scenario File**: Choose a scenario file to start the simulation. The simulation will start immediately because scenario files use a states file.

The simulation can also be exported at any time during its execution. The exported file will be saved in the `breathe.engine/states/exported` folder.



## External Ventilator Usage

In the `breathe.zeroMQ` package, there is a `zeroClient` class that serves as a demo illustrating what you can do to test an external ventilator. 
Once the server from the Swing interface is running, you can connect a client. This client will receive information about the simulation and can send a specified pressure or volume.
We have created a demo, but you can adjust the values for pressure or volume that you wish to send, depending on your ventilator. Simply modify the `processVolume` and `processPressure` classes to set the desired parameters.








