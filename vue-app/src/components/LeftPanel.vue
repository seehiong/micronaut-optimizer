<template>
  <div class="left-panel">
    <h3>Nodes</h3>
    <div v-for="node in presetNodes" :key="node.id" class="draggable-node" draggable="true"
      @dragstart="onDragStart($event, node)">
      {{ node.name }}
    </div>
  </div>
</template>

<script>
export default {
  name: "LeftPanel",
  data() {
    return {
      // Preset nodes available for dragging
      presetNodes: [
        {
          name: "Input Text",
          inputTypes: [],
          outputTypes: ["textInput"],
          triggerAction: "T",
          textInput: true,
        },
        {
          name: "Output Text",
          type: "any",
          inputTypes: ["Any"],
          outputTypes: [],
          triggerAction: "A",
          textOutput: true,
        },
        {
          name: "Matrix of Doubles",
          type: "matrix-of-doubles",
          inputTypes: ["textInput"],
          outputTypes: ["double[][]"],
          triggerAction: "A",
        },
        {
          name: "Cast to String",
          type: "cast-to-string",
          inputTypes: ["textInput"],
          outputTypes: ["string"],
          triggerAction: "A",
        },
        {
          name: "Solve Time Constraint",
          type: "json-formatter",
          inputTypes: ["string"],
          outputTypes: ["solveTimeConstraint"],
          triggerAction: "A",
          formatterKey: "solveTime",
        },
        {
          name: "Distance Matrix Constraint",
          type: "json-formatter",
          inputTypes: ["double[][]"],
          outputTypes: ["distanceMatrixConstraint"],
          triggerAction: "A",
          formatterKey: "distances",
        },
        {
          name: "TSP Input",
          type: "json-aggregator",
          inputTypes: ["distanceMatrixConstraint", "solveTimeConstraint"],
          outputTypes: ["TSPInput"],
          triggerAction: "A",
        },
        {
          name: "TSP Problem",
          type: "invoke-api",
          inputTypes: ["TSPInput"],
          outputTypes: ["TSPOutput"],
          triggerAction: "O",
          apiUrl: "/api/solve/tsp",
        },
      ],
    };
  },
  methods: {
    // Handle drag start event
    onDragStart(event, node) {
      // Add isNew flag to indicate this is a new node
      event.dataTransfer.setData("application/json", JSON.stringify({
        ...node,
        isNew: true
      }));
    },
  },
};
</script>

<style scoped>
.left-panel {
  width: 200px;
  padding: 10px;
  background-color: #f0f0f0;
  border-right: 1px solid #ccc;
}

h3 {
  margin-bottom: 10px;
}

.draggable-node {
  padding: 10px;
  margin-bottom: 5px;
  background-color: #42b983;
  color: white;
  border-radius: 5px;
  cursor: grab;
  text-align: center;
}

.draggable-node:hover {
  background-color: #ff6b6b;
}
</style>