<template>
  <div class="node" :style="{ left: x + 'px', top: y + 'px' }" :data-node-id="id" @mousedown="onStartDrag">
    <!-- Input text connector -->
    <svg v-if="textInput" class="connector-line left-connector" width="220" height="100">
      <line x1="200" y1="50" x2="220" y2="50" stroke="#42b983" stroke-width="2" />
    </svg>

    <!-- Output text connector -->
    <svg v-if="textOutput" class="connector-line right-connector" width="220" height="100">
      <line x1="0" y1="50" x2="20" y2="50" stroke="#42b983" stroke-width="2" />
    </svg>

    <!-- Input Points -->
    <BasePoint v-for="(input, index) in inputTypes" :key="'input-' + index" :id="`${id}-i${index}`" type="input"
      :nodeWidth="200" :offsetY="getPortPosition(index, inputTypes.length)" :label="input" @start-link="onStartLink" />

    <!-- Node Content -->
    <div class="node-rectangle">
      <div class="node-name" :style="{ fontSize: fontSize + 'px' }" :title="name">
        {{ truncatedName }}
      </div>
      <div class="left-section">
        <!-- Textarea for textInput nodes -->
        <textarea v-if="textInput" v-model="userInput" placeholder="Enter text here..." class="text-input-area"
          spellcheck="false" @mousedown.stop></textarea>
      </div>
      <div class="right-section">
        <!-- Trigger Button -->
        <button v-if="triggerAction !== 'A'" class="trigger-button"
          :title="triggerAction === 'T' ? 'Transform' : 'Optimize'" @click="onTrigger">
          {{ triggerAction }}
        </button>
      </div>
      <!-- Add output textarea section -->
      <div class="output-section">
        <textarea v-if="textOutput" :value="nodeDataToString" @input="onUpdateNodeData" placeholder="Output text..."
          class="text-output-area" spellcheck="false" @mousedown.stop readonly></textarea>
      </div>
    </div>

    <!-- Output Points -->
    <BasePoint v-for="(output, index) in outputTypes" :key="'output-' + index" :id="`${id}-o${index}`" type="output"
      :nodeWidth="200" :offsetY="getPortPosition(index, outputTypes.length)" :label="output"
      @start-link="onStartLink" />
  </div>
</template>

<script>
import BasePoint from "./NodePort.vue";

export default {
  name: "BaseNode",
  components: {
    BasePoint,
  },
  props: {
    id: {
      type: String,
      required: true,
    },
    name: {
      type: String,
      default: "Node Name",
    },
    x: {
      type: Number,
      required: true,
    },
    y: {
      type: Number,
      required: true,
    },
    inputTypes: {
      type: Array,
      default: () => [],
    },
    outputTypes: {
      type: Array,
      default: () => [],
    },
    textInput: {
      type: Boolean,
      default: false,
    },
    textOutput: {
      type: Boolean,
      default: false,
    },
    triggerAction: {
      type: String,
      default: "A", // auto trigger
      validator: (value) => ["T", "O", "A"].includes(value), // Validate allowed values
    },
    nodeData: { // Allow multiple types
      type: [String, Array, Object, null],
      default: null,
    },
  },
  data() {
    return {
      userInput: "", // Store input from the textarea
      maxWidth: 200, // Fixed width of the node
      minFontSize: 12, // Minimum font size
      maxFontSize: 16, // Maximum font size
      maxChars: 20, // Maximum characters before truncation
    };
  },
  computed: {
    fontSize() {
      // Calculate the font size based on the length of the node name.
      const length = this.name.length;
      if (length > this.maxChars) {
        return Math.max(this.minFontSize, this.maxFontSize - (length - this.maxChars) * 0.5);
      }
      return this.maxFontSize;
    },

    truncatedName() {
      // Truncate the node name if it exceeds the maximum characters.
      if (this.name.length > this.maxChars) {
        return this.name.slice(0, this.maxChars - 3) + "...";
      }
      return this.name;
    },

    nodeDataToString() {
      if (this.nodeData === null || this.nodeData === undefined) {
        return ""; // Return an empty string for null or undefined
      }

      if (typeof this.nodeData === "string") {
        return this.nodeData; // Return the string as is
      }

      if (Array.isArray(this.nodeData) || typeof this.nodeData === "object") {
        return JSON.stringify(this.nodeData, null, 2); // Pretty-print JSON for arrays or objects
      }

      // Fallback for other types (e.g., numbers, booleans)
      return String(this.nodeData);
    },
  },
  methods: {
    onStartDrag(event) {
      this.$emit("start-drag", this.id, event);
    },

    onStartLink(pointId, type, position) {
      // Calculate absolute position based on node position
      const absolutePosition = {
        x: position.x + this.x,
        y: position.y + this.y,
      };
      this.$emit("start-link", pointId, type, absolutePosition);
    },

    onTrigger() {
      // Determine the data to forward based on the node type
      let dataToForward;
      if (this.textInput) {
        // For nodes with a textarea, use userInput
        dataToForward = this.userInput;
      } else {
        // For other nodes, use nodeData
        dataToForward = this.nodeData;
      }
      // Emit the trigger event with the data
      this.$emit("trigger", {
        nodeId: this.id,
        action: this.triggerAction,
        data: dataToForward,
      });
    },

    onUpdateNodeData(event) {
      // Emit an event to update the nodeData prop in the parent component
      this.$emit("update-node-data", event.target.value);
    },

    getPortPosition(index, totalPorts) {
      const nodeHeight = 100; // Total height of the node
      const nodeNameHeight = 30; // Height of the node name section (adjust as needed)
      const availableHeight = nodeHeight - nodeNameHeight; // Height available for points
      const spacing = availableHeight / (totalPorts + 1); // Evenly distribute points
      return nodeNameHeight + spacing * (index + 1); // Offset by node name height
    },
  },
};
</script>

<style scoped>
.node {
  position: absolute;
  cursor: grab;
  user-select: none;
  z-index: 100;
}

.node-rectangle {
  width: 200px;
  /* Fixed width */
  height: 100px;
  background-color: #ff6b6b;
  color: white;
  display: flex;
  flex-direction: column;
  border-radius: 5px;
  position: relative;
  z-index: 1;
  overflow: visible;
  /* Allow content to extrude outside */
  position: relative;
}

.node-name {
  text-align: center;
  font-weight: bold;
  padding: 5px 0;
  background-color: rgba(0, 0, 0, 0.2);
  border-top-left-radius: 5px;
  border-top-right-radius: 5px;
  height: 30px;
  /* Fixed height */
  overflow: hidden;
  /* Ensure text doesn't overflow */
  white-space: nowrap;
  /* Prevent text from wrapping */
  text-overflow: ellipsis;
  /* Add ellipsis for overflow */
  display: flex;
  align-items: center;
  justify-content: center;
}

.left-section {
  position: absolute;
  left: -220px;
  /* Move it left of the node, with some spacing */
  top: 0;
  width: 200px;
  /* Same width as node-rectangle */
  padding: 0;
  /* Remove padding since it's now separate */
}

.right-section {
  position: absolute;
  /* Position the right section absolutely */
  top: 0;
  /* Align to the top */
  right: -15px;
  /* Move 50% outside the node */
  width: 30px;
  /* Width of the right section */
  height: 30px;
  /* Match the height of the node-name */
  display: flex;
  align-items: center;
  justify-content: flex-end;
  /* Align button to the right */
}

.trigger-button {
  background-color: #42b983;
  color: white;
  border: none;
  padding: 5px 10px;
  border-radius: 3px;
  cursor: pointer;
  font-size: 12px;
  position: relative;
  /* Ensure the button is positioned correctly */
  z-index: 2;
  /* Ensure the button is above other elements */
}

.trigger-button:hover {
  background-color: #3aa876;
}

.connector-line {
  position: absolute;
  top: 0;
  pointer-events: none;
  z-index: 0;
  /* Ensure it's behind other elements */
}

.left-connector {
  left: -220px;
}

.right-connector {
  left: 200px;
  /* Node width */
}

.output-section {
  position: absolute;
  left: 220px;
  /* Move it right of the node */
  top: 0;
  width: 200px;
}

.text-input-area {
  width: 100%;
  height: 100%;
  min-height: 80px;
  resize: vertical;
  border: 1px solid #ccc;
  border-radius: 3px;
  padding: 5px;
  font-family: inherit;
  font-size: 14px;
  box-sizing: border-box;
}

.text-output-area {
  width: 100%;
  height: 100%;
  min-height: 120px;
  resize: both;
  border: 1px solid #ccc;
  border-radius: 3px;
  padding: 5px;
  font-family: inherit;
  font-size: 14px;
  box-sizing: border-box;
  overflow: auto;
}
</style>