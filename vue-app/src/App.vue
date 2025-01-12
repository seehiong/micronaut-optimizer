<template>
  <div class="app">
    <!-- LeftPanel Component -->
    <LeftPanel ref="leftPanel" />

    <!-- MainPanel Component -->
    <MainPanel :nodes="nodes" :linking="linking" :mousePosition="mousePosition" :lines="lines"
      :leftPanelWidth="leftPanelWidth" @trigger="onTrigger" @drop="onDrop" @update-linking="onUpdateLinking"
      @add-line="onAddLine" @update-mouse-position="onUpdateMousePosition" />
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import LeftPanel from "./components/LeftPanel.vue";
import MainPanel from "./components/MainPanel.vue";

let nodeCounter = 0;

export default {
  components: {
    LeftPanel,
    MainPanel,
  },
  setup() {
    const leftPanel = ref(null);
    const leftPanelWidth = ref(0);

    onMounted(() => {
      leftPanelWidth.value = leftPanel.value.$el.offsetWidth;
    });

    return {
      leftPanel,
      leftPanelWidth,
    };
  },
  data() {
    return {
      nodes: [],
      lines: [],
      linking: {
        sourceId: null,
        sourceType: null,
        targetId: null,
        isDrawing: false,
      },
      mousePosition: { x: 0, y: 0 },
      hasOptimizationTrigger: false,
    };
  },

  methods: {
    // Handle drop event to add a new node
    onDrop(event) {
      event.preventDefault();
      const nodeData = JSON.parse(event.dataTransfer.getData("application/json"));

      // Only create new node if it's coming from LeftPanel
      if (nodeData.isNew) {
        const newNode = {
          ...nodeData,
          id: `n${nodeCounter++}`, // Use the static counter for node id
          x: event.offsetX,
          y: event.offsetY,
        };
        this.nodes.push(newNode);

      } else {
        // Handle moving existing node
        // Update position of existing node
      }
    },
    onUpdateLinking(linkingState) {
      this.linking = linkingState;
    },
    onAddLine(newLine) {
      this.lines = [...this.lines, newLine];
    },
    onUpdateMousePosition(position) {
      this.mousePosition = position;
    },
    onTrigger(nodeId, action) {
      console.log(`Trigger fired for node: ${nodeId}, Action: ${action}`);
      // Implement logic based on the action (T or O)
    },
  },
};
</script>

<style>
.app {
  display: flex;
  height: 100vh;
}
</style>