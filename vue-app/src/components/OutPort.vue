<template>
    <div class="node-port output" :style="pointStyle" :data-point-id="id" @mousedown.stop="onMouseDown">
        <span class="port-label" :title="tooltipText">{{ truncatedLabel }}</span>
    </div>
</template>

<script>
import { valueToString } from '@/utils/transformUtils';

export default {
    name: "OutPort",

    props: {
        id: { type: String, required: true },
        nodeWidth: { type: Number, required: true },
        offsetY: { type: Number, required: true },
        label: { type: String, required: true },
        outPortData: { required: false },
    },

    computed: {
        pointStyle() {
            return {
                left: `${this.nodeWidth - 5}px`,
                top: `${this.offsetY}px`,
            };
        },
        tooltipText() {
            if (this.outPortData) {
                return valueToString(this.outPortData);
            }
            return this.truncateLabel();
        },
        truncatedLabel() {
            return this.truncateLabel();
        },
    },

    methods: {
        onMouseDown() {
            const position = {
                x: this.nodeWidth,
                y: this.offsetY,
            };
            this.$emit("start-link", this.id, "output", position);
        },
        truncateLabel() {
            const maxWidth = this.nodeWidth * 0.8;
            const fontSize = 10;
            const maxChars = Math.floor(maxWidth / fontSize);
            return this.label.length > maxChars
                ? this.label.slice(0, maxChars) + "..."
                : this.label;
        },
    },
};
</script>

<style scoped>
.node-port {
    position: absolute;
    width: 10px;
    height: 10px;
    background-color: #42b983;
    border-radius: 50%;
    cursor: pointer;
    z-index: 2;
}

.port-label {
    position: absolute;
    font-size: 10px;
    color: white;
    white-space: nowrap;
}

.node-port.output .port-label {
    right: 15px;
    top: 50%;
    transform: translateY(-50%);
}
</style>