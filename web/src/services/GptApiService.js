const API_URL = "http://localhost:7070";

export class GptApiService {


    static async sendAnthropicPrompt(apiRequest, id) {

        if (id === undefined) {
            return await fetch(`${API_URL}/api/anthropic`, {
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
            })
        } else {
            return await fetch(`${API_URL}/api/conversation/${id}/anthropic`, {
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
            })
        }

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
}