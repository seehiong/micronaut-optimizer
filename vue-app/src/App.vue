<template>
  <div class="app">
    <!-- LeftPanel Component -->
    <LeftPanel :nodes="nodes" :lines="lines" @clear-state="onClearState" @load-state="onLoadState" />

    <!-- MainPanel Component -->
    <MainPanel :key="mainPanelKey" :nodes="nodes" :lines="lines" :linking="linking" :mousePosition="mousePosition"
      @drop="onDrop" @update-linking="onUpdateLinking" @add-line="onAddLine"
      @update-mouse-position="onUpdateMousePosition" />
  </div>
</template>

<script>
import { useToast } from "vue-toastification";
import { mapState, mapActions } from 'vuex';
import { Node } from "@/models/Node";
import LeftPanel from "@/components/LeftPanel.vue";
import MainPanel from "@/components/MainPanel.vue";

const toast = useToast();
const NODE_LIMIT = 20;

export default {
  components: {
    LeftPanel,
    MainPanel,
  },
  data() {
    return {
      linking: {
        sourceId: null,
        sourceType: null,
        targetId: null,
        isDrawing: false,
      },
      mousePosition: { x: 0, y: 0 },
      mainPanelKey: 0,
    };
  },
  computed: {
    ...mapState(['nodes', 'lines']), // Map the nodes and lines arrays from the Vuex store
  },
  methods: {
    ...mapActions(['updateNodes', 'updateLines', 'addLine']), // Map the actions

    onUpdateMousePosition(position) {
      this.mousePosition = position;
    },

    onUpdateLinking(linkingState) {
      this.linking = linkingState;
    },

    // Handle drop event to add a new node
    onDrop(event) {
      event.preventDefault();
      if (this.nodes.length >= NODE_LIMIT) {
        toast.warning(`Maximum limit of ${NODE_LIMIT} nodes reached!`);
        return;
      }
      const data = JSON.parse(event.dataTransfer.getData("application/json"));
      if (data.isNew) {
        const newNode = Node.fromDroppedData(data, event.x, event.y);
        this.nodes.push(newNode);
      }
    },

    // Handle adding a new line
    onAddLine(newLine) {
      this.addLine(newLine);
    },

    // Handle clearing the state
    onClearState() {
      this.updateNodes([]);
      this.updateLines([]);
      this.mainPanelKey = (this.mainPanelKey + 1) % 1000;
    },

    // Handle loading the state
    onLoadState(state) {
      const nodes = state.nodes.map(nodeData => Node.fromLoadedState(nodeData));
      Node.updateCounter(nodes);
      this.updateNodes(nodes);
      this.updateLines(state.lines);
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