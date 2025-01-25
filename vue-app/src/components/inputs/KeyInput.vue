<template>
    <textarea v-model="key" placeholder="Outer JSON key..." spellcheck="false" @mousedown.stop @mouseup.stop
        @mousemove.stop @input="onInputChange"
        :style="{ width: initialWidth + 'px', height: initialHeight + 'px' }"></textarea>
</template>

<script>
export default {
    name: "KeyInput",
    props: {
        initialData: { type: String, default: "" },
        initialWidth: { type: Number, default: 150 },
        initialHeight: { type: Number, default: 60 }
    },
    watch: {
        initialData: {
            handler(newValue) {
                if (newValue !== this.key) {
                    this.key = newValue || "";
                }
            },
            immediate: true
        },
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
    methods: {
        onInputChange(event) {
            this.key = event.target.value;
            this.$emit("input-change", this.key);
        },
    },
};
</script>

<style scoped>
textarea {
    position: absolute;
    top: 40px;
    left: -20px;
    z-index: 2;
    background-color: white;
    border: 1px solid red;
    resize: both;
    padding: 5px;
    box-sizing: border-box;
    cursor: text;
    min-width: 100px;
    max-width: 200px;
    min-height: 60px;
    max-height: 400px;
}

textarea::placeholder {
    color: #000;
    opacity: 1;
}
</style>