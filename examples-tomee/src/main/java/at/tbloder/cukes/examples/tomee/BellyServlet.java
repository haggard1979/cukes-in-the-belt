package at.tbloder.cukes.examples.tomee;

import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * {@code GET /belly} returns the number of cukes, {@code POST /belly?eat=n} eats some, {@code DELETE /belly} empties it.
 */
@WebServlet("/belly")
public class BellyServlet extends HttpServlet {
    @Inject
    private Belly belly;

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain");
        resp.getWriter().print(belly.getCukes());
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        belly.eat(Integer.parseInt(req.getParameter("eat")));
        doGet(req, resp);
    }

    @Override
    protected void doDelete(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        belly.empty();
        doGet(req, resp);
    }
}
