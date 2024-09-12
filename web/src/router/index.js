import {createRouter, createWebHashHistory} from 'vue-router'
import ChatView from "@/views/ChatView.vue";

const router = createRouter({
    // history: createWebHistory(import.meta.env.BASE_URL),
    history: createWebHashHistory(),
    routes: [
        {
            path: '/',
            name: 'home',
            component: ChatView
        },
        {
            path: '/conversations/:id',
            name: 'conversation',
            component: ChatView
        },
        {
            path: '/about',
            name: 'about',
            // route level code-splitting
            // this generates a separate chunk (About.[hash].js) for this route
            // which is lazy-loaded when the route is visited.
            component: () => import('../views/ChatView.vue')
        }
    ]
})

export default router
