<template>
  <div>
    <!-- Hamburger Icon (Extended to cover top portion) -->
    <div class="hamburger" @click="togglePanel">
      <span class="hamburger-icon">☰</span>
      <span class="hamburger-text">Menu</span>
    </div>

    <!-- Left Panel -->
    <div class="left-panel" :class="{ collapsed: isCollapsed }">
      <!-- Include LocalPersistent Component -->
      <LocalPersistent ref="localPersistent" :nodes="nodes" :lines="lines" @clear-state="onClearState"
        @load-state="onLoadState" />

      <!-- Accordion Sections -->
      <div v-for="(section, index) in sections" :key="index" class="accordion-section">
        <div class="accordion-header" @click="toggleSection(index)">
          <h3>{{ section.title }}</h3>
          <span class="arrow">{{ isOpen(index) ? '▼' : '▶' }}</span>
        </div>
        <div v-if="isOpen(index)" class="accordion-content">
          <!-- Render buttons for File Operations -->
          <div v-if="section.isFileOperations" class="file-operations">
            <button @click="onOpenSaveDialog">Save Problem</button>
            <button @click="onOpenLoadDialog">Load Problem</button>
          </div>
          <!-- Render nodes for other sections -->
          <div v-else>
            <div v-for="node in section.nodes" :key="node.id" class="draggable-node" draggable="true"
              @dragstart="onDragStart($event, node)">
              <span class="node-icon">{{ getIcon(node.iconType) }}</span>
              <span class="node-name">{{ node.name }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { Node, NodeTypes, IconType, TriggerAction } from "@/models/Node";
import LocalPersistent from "@/components/LocalPersistent.vue";

export default {
  name: "LeftPanel",
  components: {
    LocalPersistent,
  },
  props: {
    nodes: { type: Array, required: true },
    lines: { type: Array, required: true },
  },
  data() {
    return {
      isCollapsed: false, // Track if the panel is collapsed
      openSections: [], // Track open/closed state of each section
      sections: [
        {
          title: "File Operations",
          nodes: [], // No nodes, just buttons
          isFileOperations: true, // Flag to identify this section
        },
        {
          title: "User Inputs / Outputs",
          nodes: [
            new Node({
              name: NodeTypes.TEXT_INPUT,
              iconType: IconType.INPUT,
              hasTextInput: true,
              inputTypes: [],
              outputTypes: ["textInput"],
              triggerAction: TriggerAction.SUBMIT,
            }),
            new Node({
              name: NodeTypes.KEY_INPUT,
              iconType: IconType.INPUT,
              hasKeyInput: true,
              inputTypes: [],
              outputTypes: ["keyInput"],
              triggerAction: TriggerAction.SUBMIT,
            }),
            new Node({
              name: NodeTypes.SUBKEY_INPUT,
              iconType: IconType.INPUT,
              hasSubkeyInput: true,
              inputTypes: [],
              outputTypes: ["subkeyInput"],
              triggerAction: TriggerAction.SUBMIT,
            }),
            new Node({
              name: NodeTypes.TEXT_OUTPUT,
              iconType: IconType.OUTPUT,
              hasTextOutput: true,
              inputTypes: ["any"],
              outputTypes: [],
              triggerAction: TriggerAction.AUTO,
              transformType: "dump-output",
            }),
          ],
        },
        {
          title: "Transforms",
          nodes: [
            new Node({
              name: NodeTypes.MATRIX_OF_DOUBLES,
              iconType: IconType.TRANSFORM,
              inputTypes: ["textInput"],
              outputTypes: ["double[][]"],
              triggerAction: TriggerAction.AUTO,
              transformType: "matrix-of-doubles",
            }),
            new Node({
              name: NodeTypes.CAST_TO_STRING,
              iconType: IconType.TRANSFORM,
              inputTypes: ["textInput"],
              outputTypes: ["string"],
              triggerAction: TriggerAction.AUTO,
              transformType: "cast-to-string",
            }),
            new Node({
              name: NodeTypes.EXTRACT_VALUE_BY_KEY,
              iconType: IconType.TRANSFORM,
              inputTypes: ["any", "keyInput", "subkeyInput"],
              outputTypes: ["extractedValue"],
              triggerAction: TriggerAction.AUTO,
              transformType: "extract-by-key",
            }),
          ],
        },
        {
          title: "Constraints",
          nodes: [
            new Node({
              name: NodeTypes.SOLVE_TIME_CONSTRAINT,
              iconType: IconType.CONSTRAINT,
              inputTypes: ["string"],
              outputTypes: ["solveTimeConstraint"],
              triggerAction: TriggerAction.AUTO,
              transformType: "json-formatter",
              formatterKey: "solveTime",
            }),
            new Node({
              name: NodeTypes.DISTANCE_MATRIX_CONSTRAINT,
              iconType: IconType.CONSTRAINT,
              inputTypes: ["double[][]"],
              outputTypes: ["distanceMatrixConstraint"],
              triggerAction: TriggerAction.AUTO,
              transformType: "json-formatter",
              formatterKey: "distances",
            }),
          ],
        },
        {
          title: "Inputs",
          nodes: [
            new Node({
              name: NodeTypes.TSP_INPUT,
              iconType: IconType.INPUT,
              inputTypes: ["distanceMatrixConstraint", "solveTimeConstraint"],
              outputTypes: ["TSPInput"],
              triggerAction: TriggerAction.AUTO,
              transformType: "json-aggregator",
            }),
          ],
        },
        {
          title: "Problems",
          nodes: [
            new Node({
              name: NodeTypes.TSP_PROBLEM,
              iconType: IconType.PROBLEM,
              inputTypes: ["TSPInput"],
              outputTypes: ["TSPOutput"],
              triggerAction: "O",
              transformType: "invoke-api",
              apiEndpoint: "/api/solve/tsp",
            }),
            new Node({
              name: NodeTypes.TSP_GA_PROBLEM,
              iconType: IconType.PROBLEM,
              inputTypes: ["TSPInput"],
              outputTypes: ["TSPOutput"],
              triggerAction: TriggerAction.OPTIMIZE,
              transformType: "invoke-api",
              apiEndpoint: "/api/solve/tsp_ga",
            }),
          ],
        },
        {
          title: "Outputs",
          nodes: [
            new Node({
              name: NodeTypes.TSP_CHART_OUTPUT,
              iconType: IconType.OUTPUT,
              hasChartOutput: true,
              inputTypes: ["any"],
              outputTypes: [],
              triggerAction: TriggerAction.AUTO,
              transformType: "extract-solver-id",
            }),
          ],
        },
      ],
    };
  },
  methods: {
    // Handle drag start event
    onDragStart(event, node) {
      const newNode = node.clone();
      event.dataTransfer.setData("application/json", JSON.stringify({
        ...newNode,
        isNew: true,
      }));
    },
    // Toggle section open/close
    toggleSection(index) {
      if (this.isOpen(index)) {
        // If the section is already open, close it
        this.openSections = this.openSections.filter((i) => i !== index);
      } else {
        // Close all other sections and open the selected one
        this.openSections = [index];
      }
    },
    // Check if a section is open
    isOpen(index) {
      return this.openSections.includes(index);
    },
    // Toggle the left panel
    togglePanel() {
      this.isCollapsed = !this.isCollapsed;
    },
    // Get icon for node type
    getIcon(iconType) {
      const icons = {
        input: "📥",
        output: "📤",
        constraint: "⛓️",
        transform: "🔄",
        problem: "🧩",
      };
      return icons[iconType] || "🔘";
    },
    onClearState() {
      this.$emit('clear-state');
    },
    onLoadState(state) {
      this.$emit("load-state", state);
    },
    onOpenSaveDialog() {
      this.$refs.localPersistent.openSaveDialog();
    },
    onOpenLoadDialog() {
      this.$refs.localPersistent.openLoadDialog();
    },
  },
};
</script>

<style scoped>
.hamburger {
  position: fixed;
  top: 0;
  left: 0;
  width: 50px;
  height: 50px;
  background-color: #42b983;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 1000;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
  transition: background 0.3s ease;
}

.hamburger:hover {
  background-color: #3aa876;
}

.hamburger-icon {
  font-size: 24px;
}

.hamburger-text {
  display: none;
}

.left-panel {
  width: 250px;
  height: calc(100vh - 50px);
  padding: 15px;
  background-color: #f8f9fa;
  border-right: 1px solid #e0e0e0;
  box-shadow: 2px 0 5px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
  position: fixed;
  top: 50px;
  left: 0;
  z-index: 999;
  overflow-y: auto;
}

.left-panel.collapsed {
  transform: translateX(-100%);
}

.accordion-section {
  margin-bottom: 10px;
}

.accordion-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  background: linear-gradient(135deg, #42b983, #3aa876);
  color: white;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.3s ease;
}

.accordion-header:hover {
  background: linear-gradient(135deg, #3aa876, #42b983);
}

.accordion-header h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
}

.accordion-header .arrow {
  font-size: 12px;
  transition: transform 0.3s ease;
}

.accordion-content {
  padding: 10px;
  background-color: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  margin-top: 5px;
}

.draggable-node {
  display: flex;
  align-items: center;
  padding: 10px;
  margin-bottom: 5px;
  background-color: #ffffff;
  color: #333;
  border-radius: 8px;
  cursor: grab;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.draggable-node:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.node-icon {
  margin-right: 10px;
  font-size: 16px;
}

.node-name {
  font-size: 14px;
}

.file-operations {
  display: flex;
  gap: 10px;
  padding: 10px;
}

.file-operations button {
  flex: 1;
  padding: 10px;
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: background 0.3s ease;
}

.file-operations button:hover {
  background-color: #3aa876;
}
</style>