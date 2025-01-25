<template>
  <div class="output-manager">
    <!-- Text Output -->
    <TextOutput v-if="hasTextOutput" :outputData="outputData" :initialWidth="initialWidth"
      :initialHeight="initialHeight" @resize="onResize" />

    <!-- TSP Chart Output -->
    <TSPChartOutput v-if="hasTSPChart" :solverId="solverId" :initialWidth="initialWidth"
    :initialHeight="initialHeight" @resize="onResize" />
  </div>
</template>

<script>
import TextOutput from "@/components/outputs/TextOutput.vue";
import TSPChartOutput from "@/components/outputs/TSPChartOutput.vue";

export default {
  name: "OutputManager",

  components: {
    TextOutput,
    TSPChartOutput,
  },

  props: {
    hasTextOutput: { type: Boolean, default: false },
    outputData: { type: null, required: false },
    initialWidth: { type: Number, required: false },
    initialHeight: { type: Number, required: false },
    hasTSPChart: { type: Boolean, default: false },
    solverId: { type: String, required: false },
  },

  methods: {
    onResize(dimensions) {
      this.$emit("resize", dimensions);
    },
  },
};
</script>

<style scoped>
.output-manager {
  position: absolute;
  top: 40px;
  left: 40px;
  z-index: 10;
}
</style>