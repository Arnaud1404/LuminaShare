<template>
    <header class="header">
        <RouterLink :to="{name: 'home'}" class="header">Home Page</RouterLink>
        <RouterLink :to="{name: 'user'}" class="header">Utilisateur</RouterLink>
        <RouterLink :to="{name: 'users'}" class="header">Les autres</RouterLink>
        <RouterLink :to="{name: 'modif'}" class="header">Modifier</RouterLink>
        <RouterLink :to="{name: 'login'}" class="header">Connexion</RouterLink>
        <RouterLink :to="{name: 'register'}" class="header">S'inscrire</RouterLink>




    </header>

</template>

<style>

.header {
    display: flex;
    align-items: center;
    padding: .5rem 1rem;
    gap: 1rem;
    color: white;
    background: rgba(63, 63, 73, 0.349);
    border-bottom: 1px solid grey;
}


</style>