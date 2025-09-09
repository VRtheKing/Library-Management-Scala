package modules

import com.google.inject.AbstractModule
import jobs.OverdueCheckerJob
import notifications.GrpcServer
import play.api.libs.concurrent.PekkoGuiceSupport

class Module extends AbstractModule with PekkoGuiceSupport {
  override def configure(): Unit = {
    bind(classOf[OverdueCheckerJob]).asEagerSingleton()
    bind(classOf[GrpcServer]).asEagerSingleton()
  }
}
