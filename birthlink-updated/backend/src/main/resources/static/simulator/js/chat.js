/**
 * Chat rendering utilities.
 */
const ChatRenderer = {
    /**
     * Check if text contains significant Arabic characters.
     * @param {string} text - The text to check
     * @returns {boolean} - True if text is primarily Arabic
     */
    isArabic(text) {
        if (!text) return false;
        const arabicChars = (text.match(/[\u0600-\u06FF]/g) || []).length;
        return arabicChars > text.length * 0.2;
    },

    /**
     * Format a timestamp for display.
     * @param {string} timestamp - ISO timestamp string
     * @returns {string} - Formatted time string
     */
    formatTime(timestamp) {
        if (!timestamp) return '';
        const date = new Date(timestamp);
        return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    },

    /**
     * Create a message element.
     * @param {Object} message - The message object
     * @returns {HTMLElement} - The message DOM element
     */
    createMessageElement(message) {
        const div = document.createElement('div');
        div.className = `message ${message.direction.toLowerCase()}`;

        const bubble = document.createElement('div');
        bubble.className = 'message-bubble';

        if (this.isArabic(message.body)) {
            bubble.classList.add('rtl');
        }

        bubble.textContent = message.body;

        const time = document.createElement('div');
        time.className = 'message-time';
        time.textContent = this.formatTime(message.timestamp);

        div.appendChild(bubble);
        div.appendChild(time);

        return div;
    },

    /**
     * Render all messages to the chat container.
     * @param {HTMLElement} container - The chat messages container
     * @param {Array} messages - Array of message objects
     */
    renderMessages(container, messages) {
        container.innerHTML = '';

        if (!messages || messages.length === 0) {
            container.innerHTML = `
                <div class="welcome-state">
                    <div class="welcome-icon">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                            <path d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z"/>
                        </svg>
                    </div>
                    <h3>No Messages Yet</h3>
                    <p>Send your first message to start the conversation</p>
                </div>
            `;
            return;
        }

        messages.forEach(msg => {
            container.appendChild(this.createMessageElement(msg));
        });

        container.scrollTop = container.scrollHeight;
    },

    /**
     * Add a single message to the chat.
     * @param {HTMLElement} container - The chat messages container
     * @param {Object} message - The message to add
     */
    addMessage(container, message) {
        const welcome = container.querySelector('.welcome-state');
        if (welcome) {
            welcome.remove();
        }

        container.appendChild(this.createMessageElement(message));
        container.scrollTop = container.scrollHeight;
    },

    /**
     * Show loading indicator in chat.
     * @param {HTMLElement} container - The chat messages container
     */
    showLoading(container) {
        const loading = document.createElement('div');
        loading.className = 'loading';
        loading.id = 'chat-loading';
        container.appendChild(loading);
        container.scrollTop = container.scrollHeight;
    },

    /**
     * Hide loading indicator.
     * @param {HTMLElement} container - The chat messages container
     */
    hideLoading(container) {
        const loading = container.querySelector('#chat-loading');
        if (loading) {
            loading.remove();
        }
    }
};

/**
 * Outbox rendering utilities.
 */
const OutboxRenderer = {
    /**
     * Render outbox messages.
     * @param {HTMLElement} container - The outbox container
     * @param {Array} messages - Array of outbox messages
     */
    render(container, messages) {
        container.innerHTML = '';

        if (!messages || messages.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="32" height="32">
                        <path d="M22 12h-6l-2 3h-4l-2-3H2"/>
                        <path d="M5.45 5.11L2 12v6a2 2 0 002 2h16a2 2 0 002-2v-6l-3.45-6.89A2 2 0 0016.76 4H7.24a2 2 0 00-1.79 1.11z"/>
                    </svg>
                    <p>No outbound messages yet</p>
                </div>
            `;
            return;
        }

        const sorted = [...messages].sort((a, b) =>
            new Date(b.timestamp) - new Date(a.timestamp)
        );

        sorted.forEach(msg => {
            const item = document.createElement('div');
            item.className = 'outbox-item';

            const phone = document.createElement('div');
            phone.className = 'outbox-phone';
            phone.textContent = `To: ${msg.phoneNumber}`;

            const body = document.createElement('div');
            body.className = 'outbox-message';
            if (ChatRenderer.isArabic(msg.body)) {
                body.style.direction = 'rtl';
                body.style.textAlign = 'right';
            }
            body.textContent = msg.body;

            const time = document.createElement('div');
            time.className = 'outbox-time';
            time.textContent = ChatRenderer.formatTime(msg.timestamp);

            item.appendChild(phone);
            item.appendChild(body);
            item.appendChild(time);
            container.appendChild(item);
        });
    }
};
