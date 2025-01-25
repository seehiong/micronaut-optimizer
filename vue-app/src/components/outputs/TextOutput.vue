<template>
    <textarea :value="formattedOutput" placeholder="Output text..." spellcheck="false" @mousedown.stop readonly
        :style="{ width: initialWidth + 'px', height: initialHeight + 'px' }"></textarea>
</template>

<script>
import { valueToString } from '@/utils/transformUtils';

export default {
    name: "TextOutput",
    props: {
        outputData: { type: null, required: false },
        initialWidth: { type: Number, default: 200 },
        initialHeight: { type: Number, default: 80 },
    },
    watch: {
        initialWidth: {
            handler(newValue) {
                if (newValue !== this.width) {
                    this.width = newValue || "";
                }
            },
            immediate: true
        },
        initialHeight: {
            handler(newValue) {
                if (newValue !== this.height) {
                    this.height = newValue || "";
                }
            },
            immediate: true
        }
    },
    mounted() {
        this.resizeObserver = new ResizeObserver((entries) => {
            const textarea = entries[0].target;
            this.width = textarea.offsetWidth;
            this.height = textarea.offsetHeight;
            this.$emit('resize', {
                width: this.width,
                height: this.height
            });
        });

        this.resizeObserver.observe(this.$el);
    },
    beforeUnmount() {
        if (this.resizeObserver) {
            this.resizeObserver.disconnect();
        }
    },
    computed: {
        formattedOutput() {
            return valueToString(this.outputData);
        },
    },
};
</script>

<style scoped>
textarea {
    position: absolute;
    background-color: white;
    border: 1px solid red;
    resize: both;
    padding: 5px;
    box-sizing: border-box;
    cursor: text;
    min-width: 100px;
    max-width: 300px;
    min-height: 60px;
    max-height: 400px;
}

textarea::placeholder {
    color: #000;
    opacity: 1;
}
</style>