package servlets.post;

import db.database.Roles;
import db.database.User;
import db.database.User_comment;
import db.servises.CommentServ;
import servlets.account.BaseServlet;
import view.DTO.comment.CommentDTO;
import view.DTO.post.PostAllDTO;
import view.DTO.post.PostLittleDTO;
import view.controllersDTO.comment.CommentControl;
import view.controllersDTO.post.PostContAll;
import view.controllersDTO.post.PostContLittle;
import db.database.Post;
import db.servises.PostServ;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@WebServlet("/poster")
public class Poster extends servlets.post.BaseServlet {
    public static final HashMap<Integer, String> charMap =
            new HashMap<Integer, String>();

    static {
        charMap.put(34, "&quot;");    // double quote
        charMap.put(35, "&#35;");     // hash mark (no HTML named entity)
        charMap.put(38, "&amp;");     // ampersand
        charMap.put(39, "&apos;");    // apostrophe, aka single quote
        charMap.put(60, "&lt;");      // less than
        charMap.put(62, "&gt;");      // greater than
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        int id =0;
        try {
            id = Integer.valueOf(req.getParameter("post"));
        } catch ( Exception e){
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }
        try {
            if(id<1||id>postServ.count()){
                resp.sendRedirect(req.getContextPath() + "/");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(id==0) {
            id = (Integer) req.getSession().getAttribute("post_id");
        }
        req.getSession().setAttribute("post_id",id);
        PostServ postServ = new PostServ();
        postServ.connect();
        List<Post> list = new LinkedList<>();
        List<User_comment> listOfComments = null;
        List<CommentDTO> listOfCommentsDTO = new LinkedList<>();
        List<PostLittleDTO> postLittleDTOList = new LinkedList<>();
        PostAllDTO postAllLong = null;
        Post p = null;
        try {
            p = postServ.getByID(id);
            postAllLong = PostContAll.getPostAllLong(p);
            list = postServ.last(0, 3);
            listOfComments = commentServ.last(p.getId_post());
            for (User_comment c : listOfComments) {
                listOfCommentsDTO.add(CommentControl.toCommentDTO(c));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (Post post : list) {
            postLittleDTOList.add(PostContLittle.getPostLittle(post));
        }

        if (BaseServlet.checkSession(req)) {
            HttpSession session = req.getSession();
            session.setAttribute("post_id", p.getId_post());
            req.setAttribute("login", true);
        }
        req.setAttribute("postAllLong", postAllLong);
        req.setAttribute("listOfComments", listOfCommentsDTO);
        req.setAttribute("postList", postLittleDTOList);
        req.getRequestDispatcher("WEB-INF/jsp/post.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String comment = req.getParameter("comment");
        System.out.println(comment);
        StringBuilder builder = new StringBuilder();
        char[] charArray = comment.toCharArray();
        for (char nextChar : charArray) {
            String entityName = charMap.get((int) nextChar);
            if (entityName == null) {
                if (nextChar > 0x7F)
                    builder.append("&#")
                            .append(Integer.toString(nextChar, 10))
                            .append(";");
                else
                    builder.append(nextChar);
            } else
                builder.append(entityName);
        }
        CommentServ commentServ = new CommentServ();
        commentServ.connect();
        HttpSession session = req.getSession();

        int user_id = 0;
        int post_id = 0;
        boolean flagUser = false;
        boolean flagPost = false;
        try {
            user_id = (Integer) session.getAttribute("id");
            post_id = (Integer) session.getAttribute("post_id");
        } catch (NullPointerException e) {
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }
        if (user_id != 0) {
            flagUser = true;

        }
        if (post_id != 0) {
            flagPost = true;
        }

        if (flagUser && flagPost) {
            try {
                commentServ.add(new User_comment(user_id, post_id, builder.toString()));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        doGet(req,resp);
    }
}
