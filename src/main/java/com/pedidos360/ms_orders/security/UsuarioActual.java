package com.pedidos360.ms_orders.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

// Lee la identidad del usuario desde el JWT de Microsoft Entra ID.
public final class UsuarioActual {

    private UsuarioActual() {
    }

    private static JwtAuthenticationToken jwtAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth instanceof JwtAuthenticationToken jwt ? jwt : null;
    }

    // Mismo valor que account.username en MSAL (UPN / correo).
    public static String usuarioId() {
        JwtAuthenticationToken auth = jwtAuth();
        if (auth == null) {
            return null;
        }
        var jwt = auth.getToken();
        for (String claim : new String[]{"preferred_username", "upn", "unique_name", "email"}) {
            String valor = jwt.getClaimAsString(claim);
            if (valor != null && !valor.isBlank()) {
                return valor;
            }
        }
        return jwt.getSubject();
    }

    public static String email() {
        JwtAuthenticationToken auth = jwtAuth();
        if (auth == null) {
            return null;
        }
        var jwt = auth.getToken();
        for (String claim : new String[]{"email", "preferred_username", "upn", "unique_name"}) {
            String valor = jwt.getClaimAsString(claim);
            if (valor != null && valor.contains("@")) {
                return valor;
            }
        }
        return null;
    }

    // Token original, para reenviarlo al llamar a otros microservicios.
    public static String token() {
        JwtAuthenticationToken auth = jwtAuth();
        return auth != null ? auth.getToken().getTokenValue() : null;
    }
}
