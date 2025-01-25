<template>
  <div class="input-manager">
    <!-- Text Input -->
    <TextInput v-if="hasTextInput" :initialData="initialData" :initialWidth="initialWidth"
      :initialHeight="initialHeight" @input-change="onInputChange" @resize="onResize" />

    <!-- Key Input -->
    <KeyInput v-if="hasKeyInput" :initialData="initialData" :initialWidth="initialWidth" :initialHeight="initialHeight"
      @input-change="onInputChange" @resize="onResize" />

    <!-- Subkey Input -->
    <SubkeyInput v-if="hasSubkeyInput" :initialData="initialData" :initialWidth="initialWidth"
      :initialHeight="initialHeight" @input-change="onInputChange" @resize="onResize" />
  </div>
</template>

<script>
import TextInput from "@/components/inputs/TextInput.vue";
import KeyInput from "@/components/inputs/KeyInput.vue";
import SubkeyInput from "@/components/inputs/SubkeyInput.vue";

export default {
  name: "InputManager",

  components: {
    TextInput,
    KeyInput,
    SubkeyInput,
  },

  props: {
    hasTextInput: { type: Boolean, default: false },
    hasKeyInput: { type: Boolean, default: false },
    hasSubkeyInput: { type: Boolean, default: false },
    initialData: { type: null, default: "" },
    initialWidth: { type: Number, required: false },
    initialHeight: { type: Number, required: false },
  },

  methods: {
    onInputChange(value) {
      this.$emit("input-change", value);
    },
    onResize(dimensions) {
      this.$emit("resize", dimensions);
    },
  },
};
</script>

<style scoped>
.input-wrapper {
  position: absolute;
  top: 40px;
  left: -20px;
  z-index: 2;
}
</style>