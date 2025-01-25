// src/models/Node.js

export const IconType = {
    INPUT: 'input',
    OUTPUT: 'output',
    CONSTRAINT: 'constraint',
    TRANSFORM: 'transform',
    PROBLEM: 'problem'
};

export const TriggerAction = {
    SUBMIT: 'S',    // Submit action
    AUTO: 'A',      // Auto-apply action
    OPTIMIZE: 'O'   // Optimize action
};

// Node type constants
export const NodeTypes = {
    TEXT_INPUT: 'Text Input',
    KEY_INPUT: 'Key Input',
    SUBKEY_INPUT: 'Subkey Input',
    TEXT_OUTPUT: 'Text Output',
    MATRIX_OF_DOUBLES: 'Matrix of Doubles',
    CAST_TO_STRING: 'Cast to String',
    EXTRACT_VALUE_BY_KEY: 'Extract Value by Key',
    SOLVE_TIME_CONSTRAINT: 'Solve Time Constraint',
    DISTANCE_MATRIX_CONSTRAINT: 'Distance Matrix Constraint',
    TSP_INPUT: 'TSP Input',
    TSP_PROBLEM: 'TSP Problem',
    TSP_GA_PROBLEM: 'TSP-GA Problem',
    TSP_CHART_OUTPUT: 'TSP Chart Output'
};

export class Node {
    static #counter = 0;

    constructor({
        name,
        iconType,
        hasTextInput = false,
        hasKeyInput = false,
        hasSubkeyInput = false,
        hasTextOutput = false,
        hasChartOutput = false,
        inputTypes = [],
        outputTypes = [],
        triggerAction = null,
        transformType = null,
        formatterKey = null,
        apiEndpoint = null,
        id,
        x,
        y,
        inputData = [],
        outputData = "",
        width = 150,
        height = 60
    }) {
        this.id = id;
        this.name = name;
        this.iconType = iconType;
        this.hasTextInput = hasTextInput;
        this.hasKeyInput = hasKeyInput;
        this.hasSubkeyInput = hasSubkeyInput;
        this.hasTextOutput = hasTextOutput;
        this.hasChartOutput = hasChartOutput;
        this.inputTypes = inputTypes;
        this.outputTypes = outputTypes;
        this.triggerAction = triggerAction;
        this.transformType = transformType;
        this.formatterKey = formatterKey;
        this.apiEndpoint = apiEndpoint;
        this.x = x;
        this.y = y;
        this.inputData = inputData;
        this.outputData = outputData;
        this.width = width;
        this.height = height;
    }

    // Update counter based on loaded state
    static updateCounter(nodes) {
        const maxId = nodes.reduce((max, node) => {
            const idNum = parseInt(node.id.substring(1));
            return idNum > max ? idNum : max;
        }, -1);
        Node.#counter = maxId + 1;
    }

    // Get current counter
    static getCurrentCounter() {
        return Node.#counter;
    }

    // Create nodes from loaded state
    static fromLoadedState(nodeData) {
        // Check if nodeData is valid
        if (!nodeData || typeof nodeData !== 'object') {
            console.error('Invalid node data:', nodeData);
            return null;
        }
        try {
            const completeNodeData = JSON.parse(JSON.stringify(nodeData));
            const node = new Node({
                ...completeNodeData,
            });
            return node;
        } catch (error) {
            console.error('Error creating node:', error, nodeData);
            return null;
        }
    }

    // Update position and generate new ID for dropped nodes
    static fromDroppedData(data, x, y) {
        const node = new Node(data);
        node.id = node.generateId();
        node.updatePosition(x, y);
        return node;
    }

    generateId() {
        return `n${Node.#counter++}`;
    }

    clone() {
        return new Node({
            ...this
        });
    }

    updateDimensions(width, height) {
        this.width = width;
        this.height = height;
    }

    updateOutputData(outputData) {
        this.outputData = outputData;
    }

    updatePosition(x, y) {
        this.x = x;
        this.y = y;
    }
}