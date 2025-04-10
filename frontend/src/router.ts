import { createWebHistory, createRouter } from "vue-router";
import type { RouteRecordRaw } from "vue-router";

const routes: Array<RouteRecordRaw> = [
  {
    path: "/",
    name: "home",
    component: () => import("./components/Home.vue"),
    props: false
  },
  {
    path: "/user",
    name: "user",
    component: () => import("./components/User.vue"),
    props: true
  },
  {
    path: "/other_user",
    name: "users",
    component: () => import("./components/Users.vue"),
    props: false
  },
  {
    path: "/edit",
    name: "modif",
    component: () => import("./components/Edit.vue"),
    props: false
  },
  {
    path: "/register",
    name: "register",
    component: () => import("./components/Register.vue"),
    props: false
  },
  {
    path: "/login",
    name: "login",
    component: () => import("./components/Login.vue"),
    props: false
  },

  {
    path: "/:pathMatch(.*)*",
    name: "not_found",
    component: () => import("./components/NotFoundPage.vue"),
    props: false
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;