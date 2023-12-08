package com.example.parcel_delivery.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.example.parcel_delivery.utils.JWTUtils;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        // Wrapping the message to access its headers
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand cmd = accessor.getCommand();

        if (StompCommand.CONNECT.equals(cmd) || StompCommand.SEND == cmd) {

            String token = accessor.getFirstNativeHeader("Authorization");

            if (token != null && token.startsWith("Bearer ")) {
                String jwt = token.substring(7);
                String username = jwtUtils.extractUsername(jwt);

                // Validating the token and checking if the user exists
                if (username != null && jwtUtils.validateToken(jwt, userDetailsService.loadUserByUsername(username))) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    userDetails.getAuthorities();

                    accessor.setUser(authentication);

                }

            }
        }
        return message;
    }

}