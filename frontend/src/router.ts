import { createWebHistory, createRouter } from "vue-router";
import type { RouteRecordRaw } from "vue-router";

const routes: Array<RouteRecordRaw> = [
  {
    path: "/",
    name: "home",
    component: () => import("./components/Home.vue"),
    props: true
  },
  {
    path: "/gallery",
    name: "gallery",
    component: () => import("./components/Gallery.vue"),
    props: true
  },
  {
    path: "/image/:id",
    name: "image",
    component: () => import("./components/Gallery.vue"),
    props: ({ params }) => ({ id: Number(params.id) || 0 })
  },
  {
    path: "/upload",
    name: "upload",
    component: () => import("./components/LuminaShare.vue"),
    props: true
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;