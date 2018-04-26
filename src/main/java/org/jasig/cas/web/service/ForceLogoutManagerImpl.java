package org.jasig.cas.web.service;

import org.jasig.cas.ticket.ServiceTicketImpl;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.util.ApplicationContextProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(readOnly = false)
//@Component("forceLogoutManager")
public class ForceLogoutManagerImpl implements ForceLogoutManager {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private TicketRegistry ticketRegistry;
    /**
     * 登录成功，踢掉前一个相同登录的人
     *
     * @param username
     */
    public void doLogout(final String username) {
        /*TicketRegistry ticketRegistry = (TicketRegistry) ApplicationContextProvider.getApplicationContext()
                .getBean("ticketRegistry");*/
        final Collection<Ticket> ticketsInCache = ticketRegistry.getTickets();
        for (final Ticket ticket : ticketsInCache) {
            TicketGrantingTicket t = null;
            try {
                log.info("cast TicketGrantingTicketImpl");
                t = (TicketGrantingTicketImpl) ticket;
            } catch (Exception e) {
                log.error("cast TicketGrantingTicketImpl is error:", e);
                t = ((ServiceTicketImpl) ticket).getGrantingTicket();
            }
            if (t.getAuthentication().getPrincipal().getId().equals(username) && t.getId() != null) {
                /**
                 * 注销方法一 涉及到cookie的删除，但是无法获取response 该方法有待考究 未测试
                 */
                 //centralAuthenticationService.destroyTicketGrantingTicket(t.getId());

                /**
                 * 注销方法二
                 */
                t.expire();
                //t.markTicketExpired();
                ticketRegistry.deleteTicket(t.getId());
            }
        }
    }

    public void setTicketRegistry(TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }
}
