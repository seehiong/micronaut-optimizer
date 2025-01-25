// src/utils/transformUtils.js
import { useToast } from "vue-toastification";

const toast = useToast();

/**
 * Converts a value to a string representation.
 * @param {*} value - The value to convert.
 * @returns {string} - The string representation of the value.
 */
export function valueToString(value) {
    if (value === null || value === undefined) {
        return "";
    }
    if (typeof value === "string") {
        return String(value);
    }
    if (Array.isArray(value) || typeof value === "object") {
        return JSON.stringify(value, null, 2);
    }
    return String(value);
}

/**
 * Processes a matrix of doubles from a JSON string.
 * @param {string} text - The JSON string representing the matrix.
 * @returns {number[][]|null} - The processed matrix or null if invalid.
 */
export function processMatrixOfDoubles(text) {
    try {
        const matrix = JSON.parse(text);
        if (
            Array.isArray(matrix) &&
            matrix.every((row) => Array.isArray(row) && row.every((val) => typeof val === "number"))
        ) {
            return matrix; // Valid double[][]
        } else {
            throw new Error("Input must be a 2D array of numbers");
        }
    } catch (error) {
        const errorMessage = error instanceof Error ? error.message : "Input must be a 2D array of numbers";
        toast.error(`Invalid matrix input:\n${errorMessage}`);
        return null;
    }
}

/**
 * Casts a value to a string.
 * @param {any} value - The value to cast.
 * @returns {string} - The string representation of the value.
 */
export function processCastToString(value) {
    if (value === null || value === undefined) {
        return ''; // Return an empty string for null or undefined
    }
    // Handle arrays by joining elements with a space
    if (Array.isArray(value)) {
        return value.join(' ');
    }
    // Handle objects by converting them to JSON strings
    if (typeof value === 'object') {
        return JSON.stringify(value, null, 2); // Pretty-print JSON
    }
    // Handle other types (numbers, booleans, etc.) using toString
    return value.toString();
}

/**
 * Formats data into a JSON structure.
 * @param {any} data - The data to format.
 * @param {string} formatterKey - The key to use in the JSON structure.
 * @param {string} outputType - The output type to use in the JSON structure.
 * @returns {string} - The formatted JSON string.
 */
export function processJsonFormatter(data, formatterKey, outputType) {
    try {
        // Wrap the data in the desired JSON structure
        const jsonOutput = {
            [outputType]: {
                [formatterKey]: data,
            },
        };
        // Pretty-print the JSON
        return JSON.stringify(jsonOutput, null, 2);
    } catch (error) {
        toast.error("Invalid input for JSON formatter:\n", error);
        return "Invalid input for JSON formatter";
    }
}

export function processJsonAggregator(inputDataArray) {
    try {
        if (!Array.isArray(inputDataArray)) {
            throw new Error("Input must be an array of JSON objects.");
        }
        const aggregatedData = inputDataArray.reduce((acc, item) => {
            let jsonObj = item;
            // If the item is a string, try to parse it as JSON
            if (typeof item === 'string') {
                try {
                    jsonObj = JSON.parse(item);
                } catch (error) {
                    console.warn("Skipping invalid JSON string:", item);
                    return acc; // Skip this item
                }
            }
            // If the item is a valid object, merge it into the accumulator
            if (typeof jsonObj === 'object' && jsonObj !== null) {
                return { ...acc, ...jsonObj }; // Merge each object into the accumulator
            }
            console.warn("Skipping non-object item:", jsonObj);
            return acc; // Skip non-object items
        }, {}); // Start with an empty object
        return aggregatedData;
    } catch (error) {
        toast.error("Invalid input for JSON aggregator:\n", error);
        return {}; // Return an empty object in case of error
    }
}

/**
 * Processes and formats output text for display.
 * @param {any} text - The input text or data to format.
 * @returns {string} - The formatted output text.
 */
export function processOutputText(text) {
    try {
        // If the input is already a 2D array, format it directly
        if (Array.isArray(text) && text.every(row => Array.isArray(row))) {
            // Compactly format the 2D array
            return JSON.stringify(text)
                .replace(/\[\[/g, "[\t[") // Add tab after outer opening bracket
                .replace(/,/g, ", ") // Add consistent spacing after commas
                .replace(/],\s*\[/g, "],\t["); // Add tab between arrays
        }
        // If the input is a string, try to parse it as JSON
        if (typeof text === "string") {
            return text;
        }
        // For other cases, return the input as-is
        return JSON.stringify(text, null, 2);
    } catch (error) {
        toast.error("Invalid JSON input:\n", error);
        return "Invalid JSON input";
    }
}

/**
 * Extracts the solver ID from the data.
 * @param {Object} data - The data containing the solver ID.
 * @returns {Object} - An object with the solver ID or null if not found.
 */
export function extractSolverId(data) {
    // Initialize the transformed object
    let transformedData = {
        solverId: null
    };
    // Try to get solverId directly from the solverId property first
    if (data.solverId) {
        transformedData.solverId = data.solverId;
        return transformedData;
    }
    // If there's a message property, try to extract solverId from it
    if (data.message) {
        // Using regex to find UUID pattern in the message
        const match = data.message.match(/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/i);
        if (match) {
            transformedData.solverId = match[0];
            return transformedData;
        }
    }
    // Return the object with null solverId if nothing found
    return transformedData;
}

/**
 * Extracts a value from a JSON object using a key and subkey.
 * @param {Array<any>} inputData - The input data array containing [jsonInput, key, subkey].
 * @returns {any} - The extracted value or null if not found.
 */
export function extractByKey(inputData) {
    try {
        // Destructure the input data array
        const [jsonInput, key, subkey] = inputData;

        // Validate the input
        if (!jsonInput || !key || !subkey) {
            throw new Error("Missing required input: jsonInput, key, or subkey.");
        }

        // Parse the JSON input if it's a string
        const data = typeof jsonInput === 'string' ? JSON.parse(jsonInput) : jsonInput;

        // Extract the value using the key and subkey
        if (data && data[key] && typeof data[key][subkey] !== "undefined") {
            return data[key][subkey]; // Return the extracted value
        } else {
            throw new Error(`Invalid JSON structure: '${key}.${subkey}' not found.`);
        }
    } catch (error) {
        return null; // Return null if there's an error
    }
}

/**
 * Applies a transformation to the data based on the downstream node's transform type.
 * @param {object} node - The node to apply the transformation on.
 * @param {any} prevOutData - The data to transform.
 * @returns {any} - The transformed data.
 */
export function applyTransform(node, prevOutData) {
    let transformedData;
    try {
        switch (node.transformType) {
            case "matrix-of-doubles":
                transformedData = processMatrixOfDoubles(prevOutData);
                break;
            case "cast-to-string":
                transformedData = processCastToString(prevOutData);
                break;
            case "json-formatter":
                transformedData = processJsonFormatter(prevOutData, node.formatterKey, node.outputTypes[0]);
                break;
            case "json-aggregator":
                transformedData = processJsonAggregator(node.inputData);
                break;
            case "dump-output":
                transformedData = processOutputText(prevOutData, node.id);
                break;
            case "extract-solver-id":
                transformedData = extractSolverId(prevOutData);
                break;
            case "extract-by-key": {
                transformedData = extractByKey(node.inputData);
                break;
            }
            default:
                transformedData = prevOutData;
                break;
        }
        return transformedData;
    } catch (error) {
        const errorMessage = error instanceof Error ? error.message : "An error occurred during transformation.";
        toast.error(`applyTransform error:\n${errorMessage}`);
        console.error("applyTransform error:", error);
    }
    return prevOutData;
}