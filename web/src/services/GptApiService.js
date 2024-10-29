const API_URL = window.location.origin;
// const API_URL = "http://localhost:8070";

export class GptApiService {


    static async sendAnthropicPrompt(apiRequest, id) {

        let url = ""
        if (id === undefined) {
            url = `${API_URL}/api/anthropic`
        } else {
            url = `${API_URL}/api/conversation/${id}/anthropic`
        }
        return await fetch(url, {
            method: "POST",
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(apiRequest)
        }).then(response => {
            if (response.status !== 200) {
                return Promise.reject(response.json());
            }
            return response.json()
        });

    }

    static async getAllConversation() {

        return await fetch(`${API_URL}/api/conversations`, {
            method: "GET",
            headers: {
                'Content-Type': 'application/json'
            }
        }).then(response => {
            if (response.status !== 200) {
                return Promise.reject(response.json());
            }
            return response.json()
        })
    }

    static async getConversationItems(id) {

        return await fetch(`${API_URL}/api/conversation/${id}`, {
            method: "GET",
            headers: {
                'Content-Type': 'application/json'
            }
        }).then(response => {
            if (response.status !== 200) {
                return Promise.reject(response.json());
            }
            return response.json()
        })
    }

    static async editConversationTitle(conversationId, newTitle) {


        return await fetch(`${API_URL}/api/conversation/${conversationId}/title?title=${newTitle}`, {
            method: "POST",
            headers: {
                'Content-Type': 'application/json'
            }
        }).then(response => {
            if (response.status !== 200) {
                return Promise.reject("error");
            }
            return "ok"
        })

    }


    static async deleteConversation(conversationId) {

        return await fetch(`${API_URL}/api/conversation/${conversationId}`, {
            method: "DELETE",
            headers: {
                'Content-Type': 'application/json'
            }
        }).then(response => {
            if (response.status !== 200) {
                return Promise.reject("error");
            }
            return "ok"
        })

    }

    static async getSystemPrompts(conversationId) {

        return await fetch(`${API_URL}/api/systemPrompts?conversationId=${conversationId}`, {
            method: "GET",
            headers: {
                'Content-Type': 'application/json'
            }
        }).then(response => {
            if (response.status !== 200) {
                return Promise.reject(response.json());
            }
            return response.json()
        })
    }
}