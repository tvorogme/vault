import com.goto_vault.app._
import org.scalatra._
import javax.servlet.ServletContext
import com.goto_vault.app.Setup

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    context.mount(new GoToVaultServlet, "/*")
    var s = Setup
  }
}