# Micronaut Optimizer Vue App

## Project Overview

The **Micronaut Optimizer Vue App** is a frontend application designed to create, manage, and optimize workflows using a visual interface. It integrates with a backend Micronaut service to solve optimization problems, such as the Traveling Salesman Problem (TSP). The application is built using Vue.js and features a modular, component-based architecture.

## Project Structure

The folder structure of the `vue-app` is as follows:

```text
vue-app/
├── src/
│   ├── assets/
│   ├── components/
│   │   ├── inputs/
│   │   │   ├── KeyInput.vue
│   │   │   ├── SubkeyInput.vue
│   │   │   └── TextInput.vue
│   │   ├── managers/
│   │   │   ├── DragManager.vue
│   │   │   ├── InputManager.vue
│   │   │   ├── LinkManager.vue
│   │   │   ├── NodeManager.vue
│   │   │   └── OutputManager.vue
│   │   ├── outputs/
│   │   │   ├── TextOutput.vue
│   │   │   └── TSPChartOutput.vue
│   │   ├── ConnectionLine.vue
│   │   ├── Inport.vue
│   │   ├── LeftPanel.vue
│   │   ├── LocalPersistent.vue
│   │   ├── MainPanel.vue
│   │   ├── OutPort.vue
│   │   └── WorkflowNode.vue
│   ├── models/
│   │   └── Node.js
│   ├── store/
│   │   └── index.js
│   ├── utils/
│   │   ├── lineConnectionUtils.vue
│   │   ├── nodeUtils.vue
│   │   └── transformUtils.js
│   ├── App.vue
│   └── main.js
├── babel.config.js
├── jsconfig.json
├── package.json
├── README.md
└── vue.config.js
```

## Key Concepts and Features

### 1. Component-Based Architecture

* The application is built using **Vue components**, which are organized in the `src/components` directory.

* Each component is responsible for a specific functionality, promoting **modularity** and **reusability**.

* Examples:
    * `NodeManager.vue`: Manages the creation and behavior of nodes.
    * `TransformManager.vue`: Handles data transformations and API integrations.
    * `WorkflowNode.vue`: Represents individual nodes in the workflow.

### 2. Drag-and-Drop Functionality

* The application provides a **drag-and-drop interface** for creating and connecting nodes.

* Key components:
    * `DragManager.vue`: Manages drag-and-drop interactions.
    * `LinkManager.vue`: Handles the creation and management of connections between nodes.

* Users can visually design workflows by dragging nodes and connecting them.

### 3. Data Persistence with IndexedDB

* The `LocalPersistent.vue` component now uses **IndexedDB** for saving and loading the application state, replacing `localStorage`.

* This ensures that users can save their workflows and resume work later without losing progress, even with large datasets.

* Features:
    * **Compression**: Data is compressed using pako before saving to reduce storage size.
    * **Efficient Storage**: IndexedDB allows for larger datasets compared to localStorage.

### 4. API Integration

* The `TransformManager.vue` component includes methods for **processing data** and **invoking backend APIs**.

* It integrates with the Micronaut backend to solve optimization problems, such as the Traveling Salesman Problem (TSP).

* Key features:
    * **Streaming support**: Handles Server-Sent Events (SSE) for real-time updates from the backend.
    * **Error handling**: Ensures robust error handling for API requests.

### 5. Dynamic Node Management

* The `WorkflowNode.vue` component represents individual nodes in the workflow.

* Nodes can have different types, such as:
    * **Input nodes**: Provide data to the workflow.
    * **Transformation nodes**: Process data (e.g., converting text to a matrix).
    * **Output nodes**: Display the final results.

* Nodes can be dynamically added, connected, and configured.

### 6. Connection Validation

* The `ConnectionValidator.vue` component ensures that connections between nodes are **valid** based on predefined rules.

* Prevents invalid data flows and ensures the integrity of the workflow.

* Features:
    * **Type checking**: Ensures that only compatible nodes can be connected.
    * **Port validation**: Prevents multiple connections to the same input port.

### 7. Consistent Line Rendering

* The `ConnectionLine.vue` component has been updated to ensure consistent rendering of lines between nodes.

* Key improvements:
    * **Relative positioning**: Lines are now rendered relative to the SVG container, ensuring consistency across viewport changes.
    * **Dynamic updates**: Lines are re-rendered when the viewport is resized or scrolled.

### 8. Configuration

* The `vue.config.js` file includes configuration for the **development server** and **webpack**.

* It also sets up a **proxy** for API requests to the backend server, enabling seamless communication between the frontend and backend.

## Running the Application

To run the application, follow these steps:

1. **Install dependencies**:

```shell
yarn install
```

2. **Start the development server**:

```shell
yarn serve
```

3. **Build the application for production**:

```shell
yarn build
```

4. **Lint and fix files**:

```shell
yarn lint
```

## Transforming Data

### Example Workflow: Matrix Transformation

1. Attach an `Input Text` node with the following data:

```json
[[0, 10, 15, 20, 25], [10, 0, 35, 25, 20], [15, 35, 0, 30, 10], [20, 25, 30, 0, 15], [25, 20, 10, 15, 0]]
```

2. Connect it to a `Matrix of Doubles` node to validate and transform the data into a double[][] matrix.

3. Connect the output to an `Output Text` node to display the transformed data:

```json
[
    [0, 10, 15, 20, 25],
    [10, 0, 35, 25, 20],
    [15, 35, 0, 30, 10],
    [20, 25, 30, 0, 15],
    [25, 20, 10, 15, 0]
]
```

## Calling the Micronaut API

### Example Workflow: Solving TSP

1. Extend the above workflow by attaching a `Distance Matrix Constraint` node to the `Matrix of Doubles` node.

2. Connect the `Distance Matrix Constraint` node to a `TSP Input` node.

3. Connect the `TSP Input` node to the `TSP Problem` node.

4. Trigger the workflow:
    * Click the **T** (Transform) button on the `Input Text` node to propagate data to the `TSP Input` node.
    * Click the **O** (Optimize) button on the `TSP Problem` node to trigger the solving process.

5. View the output:
    * Attach an `Output Text` node to the `TSP Problem` output port to display the optimization results.

## Screenshot

![Optimizer-Vue-App](images/micronaut-optimizer-vue-app-tsp-problem.png)

## Configuration Reference

For more details on configuration, see the [Vue CLI Configuration Reference](https://cli.vuejs.org/config/).

## Key Highlights

* **Visual Workflow Design**: Intuitive drag-and-drop interface for creating and managing workflows.

* **Real-Time Optimization**: Integrates with Micronaut backend for solving complex optimization problems.

* **Modular Architecture**: Components are designed for reusability and scalability.

* **Data Persistence**: Save and load workflows using IndexedDB, even with large datasets.

* **Robust Error Handling**: Ensures smooth operation even in edge cases.

* **Consistent Line Rendering**: Lines between nodes are rendered consistently across viewport changes.