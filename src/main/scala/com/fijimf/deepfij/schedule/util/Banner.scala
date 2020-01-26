package com.fijimf.deepfij.schedule.util

import com.fijimf.deepfij.schedule.BuildInfo

object Banner {
  def banner(host:String, port:Int) =List(s"""   ____  ____  ____  ____
                      |  (    \\(  __)(  __)(  _ \\
                      |   ) D ( ) _)  ) _)  ) __/
                      |  (____/(____)(____)(__)
                      |   ____  __    __
                      |  (  __)(  ) _(  )
                      |   ) _)  )( / \\) \\
                      |  (__)  (__)\\____/
                      |  \u001b[1m${BuildInfo.name} v${BuildInfo.version}\u001b[0m
                      |  \u001b[1m${BuildInfo.builtAtString}\u001b[0m
                      |  \u001b[1mhttp://$host:${port}\u001b[0m
                      |""".stripMargin)

}
