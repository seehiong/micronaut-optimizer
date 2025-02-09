<template>
  <div class="node" :style="{ left: node.x + 'px', top: node.y + 'px' }" :data-node-id="node.id"
    @mousedown="onStartDrag">
    <!-- Node Content -->
    <div class="node-rectangle">
      <div class="node-name" :style="{ fontSize: fontSize + 'px' }" :title="node.name">
        {{ truncatedName }}
      </div>
      <div class="left-section">
      </div>
      <div class="right-section">
        <button v-if="node.triggerAction !== 'A'" class="trigger-button" :title="actionTitle" @click="onTrigger">
          {{ node.triggerAction }}
        </button>
      </div>
    </div>

    <!-- Input Manager -->
    <InputManager :id="node.id" :hasTextInput="node.hasTextInput" :hasKeyInput="node.hasKeyInput"
      :hasSubkeyInput="node.hasSubkeyInput" :initialData="node.outputData" :initialWidth="node.width"
      :initialHeight="node.height" @input-change="onInputChange" @resize="onResize" />

    <!-- Output Manager -->
    <OutputManager :hasTextOutput="node.hasTextOutput" :hasTSPChart="node.hasChartOutput" :outputData="node.outputData"
      :solverId="solverId" :initialWidth="node.width" :initialHeight="node.height" @resize="onResize" />

    <!-- Input Ports -->
    <InPort v-for="(input, index) in node.inputTypes" :key="'input-' + index"
      :id="generatePortId(node.id, 'input', index)" :nodeWidth="200"
      :offsetY="getPortPosition(index, node.inputTypes.length)" :label="input" :inPortData="portInputData[index]"
      @start-link="onStartLink" />

    <!-- Output Ports -->
    <OutPort v-for="(output, index) in node.outputTypes" :key="'output-' + index"
      :id="generatePortId(node.id, 'output', index)" :nodeWidth="200"
      :offsetY="getPortPosition(index, node.outputTypes.length)" :label="output" :outPortData="portOutputData"
      @start-link="onStartLink" />
  </div>
</template>

<script>
import { Node } from '@/models/Node';
import { generatePortId, processSubmitAction, processApiStreamResponse } from '@/utils/nodeUtils';
import { sendChartDataToOpenAI, sendChartDataToLocalLLM } from '@/utils/nodeUtils';
import InputManager from "@/components/managers/InputManager.vue";
import OutputManager from "@/components/managers/OutputManager.vue";
import InPort from "@/components/InPort.vue";
import OutPort from "@/components/OutPort.vue";

export default {
  name: "WorkflowNode",
  components: {
    InputManager,
    OutputManager,
    InPort,
    OutPort,
  },

  props: {
    node: { type: Node, required: true },
  },

  data() {
    return {
      solverId: null,
      portInputData: this.node.inputData || [],
      portOutputData: this.node.outputData || "",
      nodeConfig: {
        width: 200,
        minFontSize: 12,
        maxFontSize: 16,
        maxChars: 20,
      },
    };
  },

  computed: {
    fontSize() {
      const length = this.node.name.length;
      const { minFontSize, maxFontSize, maxChars } = this.nodeConfig;
      return length > maxChars
        ? Math.max(minFontSize, maxFontSize - (length - maxChars) * 0.5)
        : maxFontSize;
    },

    truncatedName() {
      return this.node.name.length > this.nodeConfig.maxChars
        ? this.node.name.slice(0, this.nodeConfig.maxChars - 3) + "..."
        : this.node.name;
    },

    actionTitle() {
      if (this.node.triggerAction === 'S') {
        return 'Submit';
      } else if (this.node.triggerAction === 'O') {
        return 'Optimize';
      } else if (this.node.triggerAction === 'C') {
        return 'Chat';
      }
      return 'Unknown Action';
    }

  },

  watch: {
    'node.inputData': {
      immediate: true,
      handler(newValue) {
        // Check if inputData is an array and has at least one element
        if (Array.isArray(newValue) && newValue.length > 0) {
          const firstInput = newValue[0];
          // Check if the first element is an object and contains solverId
          if (firstInput && typeof firstInput === 'object' && firstInput.solverId) {
            this.solverId = firstInput.solverId; // Update the solverId property
          }
        }
        // Update portInputData with the new value
        this.portInputData = Array.isArray(newValue) ? [...newValue] : [];
      },
      deep: true
    },
    'node.outputData': {
      immediate: true,
      handler(newValue) {
        this.portOutputData = newValue;
      }
    }
  },

  methods: {
    generatePortId,

    getChangedDataIndex(newValue, oldValue) {
      if (!oldValue || !Array.isArray(oldValue)) return 0;
      const index = newValue.findIndex((value, i) => value !== oldValue[i]);
      return index === -1 ? 0 : index;
    },

    getPortPosition(index, totalPorts) {
      const nodeHeight = 100;
      const nodeNameHeight = 30;
      const spacing = (nodeHeight - nodeNameHeight) / (totalPorts + 1);
      return nodeNameHeight + spacing * (index + 1);
    },

    onStartDrag(event) {
      this.$emit("start-drag", this.node.id, event);
    },

    onStartLink(pointId, type, position) {
      const absolutePosition = {
        x: position.x + this.node.x,
        y: position.y + this.node.y,
      };
      this.$emit("start-link", pointId, type, absolutePosition);
    },

    async onTrigger() {
      // User clicks on action button, data at outPort
      switch (this.node.triggerAction) {
        case "S":
          processSubmitAction(this.node.id, this.node.outputData);
          break;
        case "O":
          if (this.node.transformType === "invoke-api") {
            processApiStreamResponse(this.node);
          }
          break;
        case "C":
          if (this.node.transformType === "invoke-api") {
            const llmProvider = process.env.VUE_APP_LLM_PROVIDER?.trim() || "local";
            if (llmProvider === "openai") {
              await sendChartDataToOpenAI(this.node);
            } else {
              await sendChartDataToLocalLLM(this.node);
            }
          }
          break;
      }
    },

    onInputChange(value) {
      this.portOutputData = value;
      this.node.updateOutputData(value);
    },

    onResize(dimensions) {
      this.node.updateDimensions(dimensions.width, dimensions.height);
    }
  },
};
</script>

<style scoped>
.node {
  position: absolute;
  cursor: grab;
  user-select: none;
  z-index: 5;
}

.node-rectangle {
  width: 200px;
  height: 100px;
  background-color: #ff6b6b;
  color: white;
  display: flex;
  flex-direction: column;
  border-radius: 5px;
  position: relative;
  z-index: 1;
  overflow: visible;
}

.node-name {
  text-align: center;
  font-weight: bold;
  padding: 5px 0;
  background-color: rgba(0, 0, 0, 0.2);
  border-top-left-radius: 5px;
  border-top-right-radius: 5px;
  height: 30px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  display: flex;
  align-items: center;
  justify-content: center;
}

.left-section {
  position: absolute;
  left: -200px;
  top: 0;
  width: 200px;
  padding: 0;
}

.right-section {
  position: absolute;
  top: 0;
  right: -15px;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
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
  z-index: 2;
  transition: background-color 0.3s ease;
}

.trigger-button:hover {
  background-color: #2e8b57;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}
</style>