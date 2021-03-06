package servlets.account;

import db.servises.UserServ;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@WebServlet("/logout")
public class Logout extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getSession().invalidate();
        Cookie[] cookie = req.getCookies();
        if (cookie == null) {
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }
        for (Cookie c : cookie) {
            if (c.getName().equals("id")) {
                c.setMaxAge(0);
                resp.addCookie(c);
            } else if (c.getName().equals("password")) {
                c.setMaxAge(0);
                resp.addCookie(c);
            }
        }
        resp.sendRedirect(req.getContextPath() + "/");
    }
}
