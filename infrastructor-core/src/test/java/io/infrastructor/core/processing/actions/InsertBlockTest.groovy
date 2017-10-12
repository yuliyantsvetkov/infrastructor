package io.infrastructor.core.processing.actions

import org.junit.Test

public class InsertBlockTest extends ActionTestBase {
    
    @Test
    public void insertBlockAtTheBeginningOfAFile() {
        inventory.provisionAs('root') {
            task name: 'insertBlockAtTheBeginningOfAFile', actions: {
                file {
                    target  = '/test.txt'
                    content = """\
                    line 1
                    line 2
                    """
                }
                
                insertBlock {
                    target  = '/test.txt'
                    block = "line 0\n"
                    position = START
                }
                
                assert shell("cat /test.txt").output == """\
                line 0
                line 1
                line 2
                """.stripMargin().stripIndent()
            }
        }
    } 
    
    @Test
    public void insertBlockAtTheEndingOfAFile() {
        inventory.provisionAs('root') {
            task actions: {
                file {
                    target  = '/test.txt'
                    content = """\
                    line 1
                    line 2
                    """
                }
                
                insertBlock {
                    target  = '/test.txt'
                    block = "line 0\n"
                    position = END
                }
                
                assert shell("cat /test.txt").output == """\
                line 1
                line 2
                line 0
                """.stripMargin().stripIndent()
            }
        }
    } 
    
    @Test
    public void insertBlockWithoutPermissions() {
        inventory.provisionAs('devops') {
            task actions: {
                file {
                    sudo = true
                    target  = '/tmp/test.txt'
                    content = "dummy"
                    owner = 'root'
                    group = 'root'
                    mode = '0600'
                }
                
                def result = insertBlock {
                    target  = '/tmp/test.txt'
                    block = "line 0\n"
                    position = END
                }
                
                assert result.exitcode != 0
                assert result.error.find(/Permission denied/)
            }
        }
    }
    
    @Test
    public void insertBlockToUnexistedFileReturnError() {
        inventory.provisionAs('devops') {
            task actions: {
                def result = insertBlock {
                    target  = '/tmp/test.txt'
                    block = "line 0\n"
                    position = END
                }.exitcode != 0
            }
        }
    }
    
    @Test
    public void insertBlockWithUnknownOwner() {
        inventory.provisionAs('devops') {
            task actions: {
                file target: '/tmp/test.txt', content: "dummy"
                
                def result = insertBlock {
                    target  = '/tmp/test.txt'
                    block = "dummy"
                    position = END
                    owner = 'unknown'
                }
                
                assert result.exitcode != 0
                assert result.error.find(/invalid spec/)
            }
        }
    }
    
    @Test
    public void insertBlockWithUnknownGroup() {
        inventory.provisionAs('devops') {
            task actions: {
                file target: '/tmp/test.txt', content: "dummy"
                
                def result = insertBlock {
                    target  = '/tmp/test.txt'
                    block = "dummy"
                    position = END
                    group = 'unknown'
                }
                
                assert result.exitcode != 0
                assert result.error.find(/invalid group/)
            }
        }
    }
    
    @Test
    public void insertBlockWithInvalidMode() {
        inventory.provisionAs('devops') {
            task actions: {
                file target: '/tmp/test.txt', content: "dummy"
                
                def result = insertBlock {
                    target  = '/tmp/test.txt'
                    block = "dummy"
                    position = END
                    mode = '888'
                }
                
                assert result.exitcode != 0
                assert result.error.find(/invalid mode/)
            }
        }
    }
}

