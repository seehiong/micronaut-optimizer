<template>
  <div class="chart-output" :style="{ width: width + 'px', height: height + 'px' }" @mousedown="startDrag">
    <canvas ref="chartCanvas"></canvas>
    <div class="resize-handle" @mousedown="startResize"></div>
  </div>
</template>

<script>
import { Chart, registerables } from 'chart.js';
import { useToast } from 'vue-toastification';

const toast = useToast();

export default {
  name: 'TSPChartOutput',
  props: {
    solverId: { type: String, default: "" },
    initialWidth: { type: Number, default: 300 },
    initialHeight: { type: Number, default: 150 },
  },
  data() {
    return {
      width: this.initialWidth,
      height: this.initialHeight,
      x: 0,
      y: 0,
      isDragging: false,
      isResizing: false,
      startX: 0,
      startY: 0,
      startWidth: 0,
      startHeight: 0,
      hasError: false,
      retryCount: 0,
      maxRetries: 5,
    };
  },
  chartInstance: null,
  eventSource: null,
  mounted() {
    this.initializeChart();
    if (this.solverId) {
      this.fetchLatestData();
      this.setupEventSource();
    }

    // Add global event listeners for dragging and resizing
    window.addEventListener('mousemove', this.onDrag);
    window.addEventListener('mouseup', this.stopDrag);
    window.addEventListener('mousemove', this.onResize);
    window.addEventListener('mouseup', this.stopResize);
  },
  beforeUnmount() {
    if (this.chartInstance) {
      this.chartInstance.destroy();
    }
    if (this.eventSource) {
      this.eventSource.close(); // Close the SSE connection
    }
    window.removeEventListener('mousemove', this.onDrag);
    window.removeEventListener('mouseup', this.stopDrag);
    window.removeEventListener('mousemove', this.onResize);
    window.removeEventListener('mouseup', this.stopResize);
  },
  watch: {
    solverId(newSolverId) {
      if (newSolverId) {
        this.fetchLatestData();
        this.setupEventSource();
      } else {
        if (this.eventSource) {
          this.eventSource.close();
        }
      }
    },
    initialWidth(newWidth) {
      if (newWidth !== this.width) {
        this.width = newWidth;
      }
    },
    initialHeight(newHeight) {
      if (newHeight !== this.height) {
        this.height = newHeight;
      }
    },
  },
  methods: {
    initializeChart() {
      const ctx = this.$refs.chartCanvas.getContext('2d');
      this.$refs.chartCanvas.width = this.width;
      this.$refs.chartCanvas.height = this.height;
      Chart.register(...registerables);
      this.chartInstance = new Chart(ctx, {
        type: 'scatter',
        data: {
          labels: [],
          datasets: [
            {
              label: 'Cities',
              data: [],
              borderColor: 'rgba(0, 0, 0, 0)',
              backgroundColor: 'rgba(255, 99, 132, 1)',
              pointRadius: 5,
              showLine: false,
            },
            {
              label: 'TSP Path',
              data: [],
              borderColor: 'rgba(54, 162, 235, 1)',
              borderWidth: 2,
              fill: false,
              showLine: true,
              pointRadius: 0,
            },
            {
              label: 'Starting Point',
              data: [],
              borderColor: 'rgba(0, 0, 0, 1)',
              backgroundColor: 'rgba(0, 255, 0, 1)',
              pointRadius: 10,
              showLine: false,
            },
          ],
        },
        options: {
          responsive: false,
          maintainAspectRatio: false,
          scales: {
            x: {
              type: 'linear',
              position: 'bottom',
              ticks: {
                callback: function (value) {
                  return Number(value).toFixed(1);
                },
              },
            },
            y: {
              type: 'linear',
              ticks: {
                callback: function (value) {
                  return Number(value).toFixed(1);
                },
              },
            },
          },
        },
      });
    },

    fetchLatestData() {
      if (!this.solverId) return;
      fetch(`/api/progress/latest/${this.solverId}`)
        .then((response) => response.json())
        .then((data) => {
          this.updateChart(data);
        })
        .catch((error) => {
          toast.error(`Error fetching latest data: ${error.message}`);
        });
    },

    setupEventSource() {
      if (!this.solverId) return;
      this.eventSource = new EventSource(`/api/progress/${this.solverId}`);
      this.eventSource.onmessage = (event) => {
        const data = JSON.parse(event.data);
        if (data.message === "complete") {
          toast.success(`Solving completed, closing event source`);
          this.eventSource.close();
          this.eventSource = null;
        } else {
          this.updateChart(data);
        }
      };
    },

    updateChart(data) {
      const cities = data?.citiesMetadata?.cities;
      const tours = data?.tourMetric?.tours;
      if (!cities || !tours) {
        return;
      }

      const cityCoordinates = cities.map((coord) => ({ x: coord[0], y: coord[1] }));
      const routeCoordinates = tours.map((index) => cities[index]).map((coord) => ({ x: coord[0], y: coord[1] }));
      routeCoordinates.push(routeCoordinates[0]);

      if (this.chartInstance) {
        this.chartInstance.data.labels = cityCoordinates.map((_, index) => `City ${index + 1}`);
        this.chartInstance.data.datasets[0].data = cityCoordinates;
        this.chartInstance.data.datasets[1].data = routeCoordinates;
        this.chartInstance.data.datasets[2].data = [cityCoordinates[0]];

        const allCoordinates = [...cityCoordinates, ...routeCoordinates];
        const xValues = allCoordinates.map((coord) => coord.x);
        const yValues = allCoordinates.map((coord) => coord.y);

        const xMin = Math.min(...xValues);
        const xMax = Math.max(...xValues);
        const yMin = Math.min(...yValues);
        const yMax = Math.max(...yValues);

        const padding = 1;
        this.chartInstance.options.scales.x.min = xMin - padding;
        this.chartInstance.options.scales.x.max = xMax + padding;
        this.chartInstance.options.scales.y.min = yMin - padding;
        this.chartInstance.options.scales.y.max = yMax + padding;

        this.chartInstance.update();
      }
    },

    startDrag(event) {
      if (event.target === this.$el) {
        event.stopPropagation();
        this.isDragging = true;
        this.startX = event.clientX - this.x;
        this.startY = event.clientY - this.y;
      }
    },

    onDrag(event) {
      if (this.isDragging) {
        this.x = event.clientX - this.startX;
        this.y = event.clientY - this.startY;
      }
    },

    stopDrag() {
      this.isDragging = false;
    },

    startResize(event) {
      event.stopPropagation();
      this.isResizing = true;
      this.startX = event.clientX;
      this.startY = event.clientY;
      this.startWidth = this.width;
      this.startHeight = this.height;
      event.preventDefault();
    },

    onResize(event) {
      if (this.isResizing) {
        const deltaX = event.clientX - this.startX;
        const deltaY = event.clientY - this.startY;
        this.width = this.startWidth + deltaX;
        this.height = this.startHeight + deltaY;

        this.$refs.chartCanvas.width = this.width;
        this.$refs.chartCanvas.height = this.height;

        if (this.chartInstance) {
          this.chartInstance.resize();
        }
        this.$emit('resize', { width: this.width, height: this.height });
      }
    },

    stopResize() {
      this.isResizing = false;
    },
  },
};
</script>

<style scoped>
.chart-output {
  position: absolute;
  border: 1px solid #ccc;
  background-color: white;
  cursor: grab;
  user-select: none;
}

.chart-output:active {
  cursor: grabbing;
}

.resize-handle {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 10px;
  height: 5px;
  background-color: #b94274;
  cursor: se-resize;
}
</style>