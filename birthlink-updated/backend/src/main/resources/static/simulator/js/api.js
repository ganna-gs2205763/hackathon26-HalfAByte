/**
 * API client for the SMS simulator.
 */
const SimulatorAPI = {
    baseUrl: '/api/simulator',

    /**
     * Send a simulated SMS message.
     * @param {string} phoneNumber - The sender's phone number
     * @param {string} body - The message body
     * @returns {Promise<{userMessage: Object, systemResponse: Object}>}
     */
    async sendMessage(phoneNumber, body) {
        const response = await fetch(`${this.baseUrl}/send`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ phoneNumber, body }),
        });

        if (!response.ok) {
            const error = await response.json().catch(() => ({ message: 'Unknown error' }));
            throw new Error(error.message || `HTTP ${response.status}`);
        }

        return response.json();
    },

    /**
     * Get conversation history for a phone number.
     * @param {string} phoneNumber - The phone number
     * @returns {Promise<{phoneNumber: string, messages: Array}>}
     */
    async getConversation(phoneNumber) {
        const encodedPhone = encodeURIComponent(phoneNumber);
        const response = await fetch(`${this.baseUrl}/conversations/${encodedPhone}`);

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        return response.json();
    },

    /**
     * Get all known phone devices.
     * @returns {Promise<Array<{phoneNumber: string, label: string, messageCount: number}>>}
     */
    async getDevices() {
        const response = await fetch(`${this.baseUrl}/devices`);

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        return response.json();
    },

    /**
     * Get all outbox messages.
     * @returns {Promise<Array<Object>>}
     */
    async getOutbox() {
        const response = await fetch(`${this.baseUrl}/outbox`);

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        return response.json();
    },

    /**
     * Reset the simulator (clear all conversations and outbox).
     * @returns {Promise<{status: string, message: string}>}
     */
    async reset() {
        const response = await fetch(`${this.baseUrl}/reset`, {
            method: 'DELETE',
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        return response.json();
    }
};
